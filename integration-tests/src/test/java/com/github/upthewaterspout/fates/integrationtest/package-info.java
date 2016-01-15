/*
 * Copyright 2018 Dan Smith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Integration tests of the testing framework. This package contains tests of both threadsafe
 * and un-threadsafe code that verify that the testing framework appropriately catches
 * threading failures.
 *
 * These tests are not in the {@link com.github.upthewaterspout.fates.core} package because that is
 * treated specially by the bytecode transformer
 */
package com.github.upthewaterspout.fates.integrationtest;