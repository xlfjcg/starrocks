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

#include <iostream>
#include <orc/OrcFile.hh>

namespace starrocks::vectorized {

class MemoryOutputStream : public orc::OutputStream {
public:
    MemoryOutputStream(ssize_t capacity) : name("MemoryOutputStream") {
        data = new char[capacity];
        length = 0;
    }

    virtual ~MemoryOutputStream() override;

    virtual uint64_t getLength() const override { return length; }

    virtual uint64_t getNaturalWriteSize() const override { return naturalWriteSize; }

    virtual void write(const void* buf, size_t size) override;

    virtual const std::string& getName() const override { return name; }

    const char* getData() const { return data; }

    void close() override {}

    void reset() { length = 0; }

private:
    char* data;
    std::string name;
    uint64_t length, naturalWriteSize;
};
} // namespace starrocks::vectorized
