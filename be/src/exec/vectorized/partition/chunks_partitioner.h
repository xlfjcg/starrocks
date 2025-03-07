// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#pragma once

#include <any>
#include <queue>

#include "column/chunk.h"
#include "column/vectorized_fwd.h"
#include "common/statusor.h"
#include "exprs/expr_context.h"
#include "partition_hash_variant.h"
#include "runtime/current_thread.h"

namespace starrocks::vectorized {

struct PartitionColumnType {
    TypeDescriptor result_type;
    bool is_nullable;
};

class ChunksPartitioner;

using ChunksPartitionerPtr = std::shared_ptr<ChunksPartitioner>;

class ChunksPartitioner {
public:
    ChunksPartitioner(const bool has_nullable_partition_column, const std::vector<ExprContext*>& partition_exprs,
                      std::vector<PartitionColumnType> partition_types);

    Status prepare(RuntimeState* state);

    // Chunk is divided into multiple parts by partition columns,
    // and each partition corresponds to a key-value pair in the hash map
    Status offer(const ChunkPtr& chunk);

    // Number of partitions
    int32_t num_partitions();

    bool is_downgrade() const { return _is_downgrade; }

    bool is_downgrade_buffer_empty() const { return _downgrade_buffer.empty(); }

    // Consumers consume from the hash map
    // method signature is: bool consumer(int32_t partition_idx, const ChunkPtr& chunk)
    // The return value of the consumer denote whether to continue or not
    template <typename Consumer>
    Status consume_from_hash_map(Consumer&& consumer) {
        // First, fetch chunks from hash map
        if (false) {
        }
#define HASH_MAP_METHOD(NAME)                                                                                     \
    else if (_hash_map_variant.type == PartitionHashMapVariant::Type::NAME) {                                     \
        TRY_CATCH_BAD_ALLOC(_fetch_chunks_from_hash_map<typename decltype(_hash_map_variant.NAME)::element_type>( \
                *_hash_map_variant.NAME, consumer));                                                              \
    }
        APPLY_FOR_PARTITION_VARIANT_ALL(HASH_MAP_METHOD)
#undef HASH_MAP_METHOD

        // Second, fetch chunks from null_key_value if any
        if (false) {
        }
#define HASH_MAP_METHOD(NAME)                                                                                          \
    else if (_hash_map_variant.type == PartitionHashMapVariant::Type::NAME) {                                          \
        TRY_CATCH_BAD_ALLOC(fetch_chunks_from_null_key_value<typename decltype(_hash_map_variant.NAME)::element_type>( \
                *_hash_map_variant.NAME, consumer));                                                                   \
    }
        APPLY_FOR_PARTITION_VARIANT_NULL(HASH_MAP_METHOD)
#undef HASH_MAP_METHOD

        return Status::OK();
    }

    // Fetch one chunk from downgrade buffer if any
    ChunkPtr consume_from_downgrade_buffer();

private:
    bool _is_partition_columns_fixed_size(const std::vector<ExprContext*>& partition_expr_ctxs,
                                          const std::vector<PartitionColumnType>& partition_types, size_t* max_size,
                                          bool* has_null);
    void _init_hash_map_variant();

    template <typename HashMapWithKey>
    void _split_chunk_by_partition(HashMapWithKey& hash_map_with_key, const ChunkPtr& chunk) {
        _is_downgrade = hash_map_with_key.append_chunk(chunk, _partition_columns, _mem_pool.get(), _obj_pool);
        if (_is_downgrade) {
            std::lock_guard<std::mutex> l(_buffer_lock);
            _downgrade_buffer.push(chunk);
        }
    }

    // Fetch chunks from hash map, return true if reaches eos
    template <typename HashMapWithKey, typename Consumer>
    bool _fetch_chunks_from_hash_map(HashMapWithKey& hash_map_with_key, Consumer&& consumer) {
        if (_hash_map_eos) {
            return true;
        }
        if (!_partition_it.has_value()) {
            _partition_it = hash_map_with_key.hash_map.begin();
            _partition_idx = 0;
        }

        using PartitionIterator = typename HashMapWithKey::Iterator;
        PartitionIterator partition_it = std::any_cast<PartitionIterator>(_partition_it);
        const PartitionIterator partition_end = hash_map_with_key.hash_map.end();

        using ChunkIterator = typename std::vector<ChunkPtr>::iterator;
        ChunkIterator chunk_it;
        DeferOp defer([&]() {
            if (partition_it == partition_end) {
                _hash_map_eos = true;
                _partition_it.reset();
                _chunk_it.reset();
            } else {
                _partition_it = partition_it;
                _chunk_it = chunk_it;
            }
        });

        while (partition_it != partition_end) {
            std::vector<ChunkPtr>& chunks = partition_it->second->chunks;
            if (!_chunk_it.has_value()) {
                _chunk_it = chunks.begin();
            }

            chunk_it = std::any_cast<ChunkIterator>(_chunk_it);
            auto chunk_end = chunks.end();

            while (chunk_it != chunk_end) {
                if (!consumer(_partition_idx, *chunk_it++)) {
                    return false;
                }
            }

            // Move to next partition
            ++partition_it;
            ++_partition_idx;
            _chunk_it.reset();
        }

        return true;
    }

    // Fetch chunks from HashMapWithKey.null_key_value, return true if reaches eos
    template <typename HashMapWithKey, typename Consumer>
    bool fetch_chunks_from_null_key_value(HashMapWithKey& hash_map_with_key, Consumer&& consumer) {
        if (_null_key_eos) {
            return true;
        }

        std::vector<ChunkPtr>& chunks = hash_map_with_key.null_key_value.chunks;

        if (!_chunk_it.has_value()) {
            _chunk_it = chunks.begin();
        }

        using ChunkIterator = typename std::vector<ChunkPtr>::iterator;
        auto chunk_it = std::any_cast<ChunkIterator>(_chunk_it);
        const auto chunk_end = chunks.end();

        DeferOp defer([&]() {
            if (chunk_it == chunk_end) {
                _null_key_eos = true;
                _chunk_it.reset();
            } else {
                _chunk_it = chunk_it;
            }
        });

        while (chunk_it != chunk_end) {
            // Because we first fetch chunks from hash_map, so the _partition_idx here
            // is already set to hash_map.size()
            if (!consumer(_partition_idx, *chunk_it++)) {
                return false;
            }
        }
        return true;
    }

    const bool _has_nullable_partition_column;
    const std::vector<ExprContext*> _partition_exprs;
    const std::vector<PartitionColumnType> _partition_types;

    RuntimeState* _state = nullptr;
    std::unique_ptr<MemPool> _mem_pool = nullptr;
    ObjectPool* _obj_pool = nullptr;

    Columns _partition_columns;
    // Hash map which holds chunks of different partitions
    PartitionHashMapVariant _hash_map_variant;

    bool _is_downgrade = false;
    // We simply buffer chunks when partition cardinality is high
    std::queue<ChunkPtr> _downgrade_buffer;
    std::mutex _buffer_lock;

    // Iterator of partitions
    std::any _partition_it;
    // Iterator of chunks of current partition
    std::any _chunk_it;
    // Offset of current consuming partition
    int32_t _partition_idx = -1;
    bool _hash_map_eos = false;
    bool _null_key_eos = false;
};
} // namespace starrocks::vectorized
