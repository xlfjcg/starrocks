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


package com.starrocks.sql.optimizer.task;

import com.google.common.base.Preconditions;
import com.starrocks.sql.optimizer.ExpressionContext;
import com.starrocks.sql.optimizer.GroupExpression;
import com.starrocks.sql.optimizer.statistics.Statistics;
import com.starrocks.sql.optimizer.statistics.StatisticsCalculator;

/**
 * DeriveStatsTask derives any stats needed for costing a GroupExpression.
 */
public class DeriveStatsTask extends OptimizerTask {
    private final GroupExpression groupExpression;

    public DeriveStatsTask(TaskContext context, GroupExpression expression) {
        super(context);
        this.groupExpression = expression;
    }

    @Override
    public String toString() {
        return "DeriveStatsTask for groupExpression " + groupExpression;
    }

    @Override
    public void execute() {
        if (groupExpression.isStatsDerived() || groupExpression.isUnused()) {
            return;
        }

        for (int i = groupExpression.arity() - 1; i >= 0; --i) {
            GroupExpression childExpression = groupExpression.getInputs().get(i).getFirstLogicalExpression();
            Preconditions.checkState(childExpression.isStatsDerived());
            Preconditions.checkNotNull(groupExpression.getInputs().get(i).getStatistics());
        }

        ExpressionContext expressionContext = new ExpressionContext(groupExpression);
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator(expressionContext,
                context.getOptimizerContext().getColumnRefFactory(), context.getOptimizerContext());
        statisticsCalculator.estimatorStats();

        Statistics currentStatistics = groupExpression.getGroup().getStatistics();
        // choose best statistics
        if (currentStatistics == null ||
                (expressionContext.getStatistics().getOutputRowCount() < currentStatistics.getOutputRowCount())) {
            groupExpression.getGroup().setStatistics(expressionContext.getStatistics());
        }

        groupExpression.setStatsDerived();
    }
}