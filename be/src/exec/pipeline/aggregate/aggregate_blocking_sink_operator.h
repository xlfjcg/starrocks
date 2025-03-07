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

#include <utility>

#include "exec/pipeline/operator.h"
#include "exec/vectorized/aggregator.h"

namespace starrocks::pipeline {
class AggregateBlockingSinkOperator : public Operator {
public:
    AggregateBlockingSinkOperator(OperatorFactory* factory, int32_t id, int32_t plan_node_id, int32_t driver_sequence,
                                  AggregatorPtr aggregator)
            : Operator(factory, id, "aggregate_blocking_sink", plan_node_id, driver_sequence),
              _aggregator(std::move(aggregator)) {
        _aggregator->set_aggr_phase(AggrPhase2);
        _aggregator->ref();
    }
    ~AggregateBlockingSinkOperator() override = default;

    bool has_output() const override { return false; }
    bool need_input() const override { return !is_finished(); }
    bool is_finished() const override { return _is_finished || _aggregator->is_finished(); }
    Status set_finishing(RuntimeState* state) override;

    Status prepare(RuntimeState* state) override;
    void close(RuntimeState* state) override;

    StatusOr<vectorized::ChunkPtr> pull_chunk(RuntimeState* state) override;
    Status push_chunk(RuntimeState* state, const vectorized::ChunkPtr& chunk) override;
    Status reset_state(RuntimeState* state, const std::vector<ChunkPtr>& refill_chunks) override;

private:
    // It is used to perform aggregation algorithms shared by
    // AggregateBlockingSourceOperator. It is
    // - prepared at SinkOperator::prepare(),
    // - reffed at constructor() of both sink and source operator,
    // - unreffed at close() of both sink and source operator.
    AggregatorPtr _aggregator = nullptr;
    // Whether prev operator has no output
    bool _is_finished = false;
};

class AggregateBlockingSinkOperatorFactory final : public OperatorFactory {
public:
    AggregateBlockingSinkOperatorFactory(int32_t id, int32_t plan_node_id, AggregatorFactoryPtr aggregator_factory)
            : OperatorFactory(id, "aggregate_blocking_sink", plan_node_id),
              _aggregator_factory(std::move(aggregator_factory)) {}

    ~AggregateBlockingSinkOperatorFactory() override = default;

    OperatorPtr create(int32_t degree_of_parallelism, int32_t driver_sequence) override {
        return std::make_shared<AggregateBlockingSinkOperator>(this, _id, _plan_node_id, driver_sequence,
                                                               _aggregator_factory->get_or_create(driver_sequence));
    }

private:
    AggregatorFactoryPtr _aggregator_factory = nullptr;
};
} // namespace starrocks::pipeline
