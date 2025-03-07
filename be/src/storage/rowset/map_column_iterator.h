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

#include "storage/rowset/column_iterator.h"

namespace starrocks {

class MapColumnIterator final : public ColumnIterator {
public:
    MapColumnIterator(ColumnIterator* null_iterator, ColumnIterator* offsets_iterator, ColumnIterator* keys_iterator,
                      ColumnIterator* values_iterator);

    ~MapColumnIterator() override = default;

    Status init(const ColumnIteratorOptions& opts) override;

    Status next_batch(size_t* n, vectorized::Column* dst) override;

    Status next_batch(const vectorized::SparseRange& range, vectorized::Column* dst) override;

    Status seek_to_first() override;

    Status seek_to_ordinal(ordinal_t ord) override;

    ordinal_t get_current_ordinal() const override { return _offsets->get_current_ordinal(); }

    /// for vectorized engine
    Status get_row_ranges_by_zone_map(const std::vector<const vectorized::ColumnPredicate*>& predicates,
                                      const vectorized::ColumnPredicate* del_predicate,
                                      vectorized::SparseRange* row_ranges) override {
        CHECK(false) << "array column does not has zone map index";
        return Status::OK();
    }

    Status fetch_values_by_rowid(const rowid_t* rowids, size_t size, vectorized::Column* values) override;

private:
    std::unique_ptr<ColumnIterator> _nulls;
    std::unique_ptr<ColumnIterator> _offsets;
    std::unique_ptr<ColumnIterator> _keys;
    std::unique_ptr<ColumnIterator> _values;
};

} // namespace starrocks
