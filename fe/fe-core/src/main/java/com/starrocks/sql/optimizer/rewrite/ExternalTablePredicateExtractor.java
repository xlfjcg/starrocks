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


package com.starrocks.sql.optimizer.rewrite;

import com.starrocks.sql.optimizer.Utils;
import com.starrocks.sql.optimizer.operator.OperatorType;
import com.starrocks.sql.optimizer.operator.scalar.BetweenPredicateOperator;
import com.starrocks.sql.optimizer.operator.scalar.BinaryPredicateOperator;
import com.starrocks.sql.optimizer.operator.scalar.CastOperator;
import com.starrocks.sql.optimizer.operator.scalar.ColumnRefOperator;
import com.starrocks.sql.optimizer.operator.scalar.CompoundPredicateOperator;
import com.starrocks.sql.optimizer.operator.scalar.ConstantOperator;
import com.starrocks.sql.optimizer.operator.scalar.InPredicateOperator;
import com.starrocks.sql.optimizer.operator.scalar.IsNullPredicateOperator;
import com.starrocks.sql.optimizer.operator.scalar.ScalarOperator;
import com.starrocks.sql.optimizer.operator.scalar.ScalarOperatorVisitor;

import java.util.LinkedList;
import java.util.List;

// Extract predicates that can be pushed down to external table
// and predicates that must be reserved
// from the entire predicate
// To be safe, we only allow push down simple  predicates
public class ExternalTablePredicateExtractor {
    private List<ScalarOperator> pushedPredicates = new LinkedList<>();
    private List<ScalarOperator> reservedPredicates = new LinkedList<>();

    public ScalarOperator getPushPredicate() {
        return Utils.compoundAnd(pushedPredicates);
    }

    public ScalarOperator getReservePredicate() {
        return Utils.compoundAnd(reservedPredicates);
    }

    public void extract(ScalarOperator op) {
        pushedPredicates.clear();
        reservedPredicates.clear();

        if (op.getOpType().equals(OperatorType.COMPOUND)) {
            CompoundPredicateOperator operator = (CompoundPredicateOperator) op;
            switch (operator.getCompoundType()) {
                case AND: {
                    List<ScalarOperator> conjuncts = Utils.extractConjuncts(operator);
                    // for CNF, we can push down each predicate independently
                    for (ScalarOperator conjunct : conjuncts) {
                        if (conjunct.accept(new CanFullyPushDownVisitor(), null)) {
                            pushedPredicates.add(conjunct);
                        } else {
                            reservedPredicates.add(conjunct);
                        }
                    }
                    return;
                }
                case OR: {
                    // for DNF, pushdown is only possible if all children can be pushed down
                    for (ScalarOperator child : operator.getChildren()) {
                        if (!child.accept(new CanFullyPushDownVisitor(), null)) {
                            reservedPredicates.add(op);
                            return;
                        }
                    }
                    pushedPredicates.add(operator);
                    return;
                }
                case NOT: {
                    if (op.getChild(0).accept(new CanFullyPushDownVisitor(), null)) {
                        pushedPredicates.add(op);
                    } else {
                        reservedPredicates.add(op);
                    }

                    return;
                }
            }
            return;
        }
        if (op.accept(new CanFullyPushDownVisitor(), null)) {
            pushedPredicates.add(op);
        } else {
            reservedPredicates.add(op);
        }
    }

    // check whether a predicate can be pushed down as a whole
    private static class CanFullyPushDownVisitor extends ScalarOperatorVisitor<Boolean, Void> {
        public CanFullyPushDownVisitor() {
        }

        private Boolean visitAllChildren(ScalarOperator op, Void context) {
            for (ScalarOperator child : op.getChildren()) {
                if (!child.accept(this, context)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Boolean visit(ScalarOperator scalarOperator, Void context) {
            return false;
        }

        @Override
        public Boolean visitConstant(ConstantOperator op, Void context) {
            return true;
        }

        @Override
        public Boolean visitVariableReference(ColumnRefOperator op, Void context) {
            return true;
        }

        @Override
        public Boolean visitBetweenPredicate(BetweenPredicateOperator op, Void context) {
            return visitAllChildren(op, context);
        }

        @Override
        public Boolean visitBinaryPredicate(BinaryPredicateOperator op, Void context) {
            return visitAllChildren(op, context);
        }

        @Override
        public Boolean visitCompoundPredicate(CompoundPredicateOperator op, Void context) {
            return visitAllChildren(op, context);
        }

        @Override
        public Boolean visitInPredicate(InPredicateOperator op, Void context) {
            return visitAllChildren(op, context);
        }

        @Override
        public Boolean visitIsNullPredicate(IsNullPredicateOperator op, Void context) {
            return visitAllChildren(op, context);
        }

        @Override
        public Boolean visitCastOperator(CastOperator op, Void context) {
            return visitAllChildren(op, context);
        }
    }
}
