/*
 * Stratio Meta
 *
 * Copyright (c) 2014, Stratio, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package com.stratio.meta.core.validator.statements;


import com.stratio.meta.core.validator.BasicValidatorTest;
import org.testng.annotations.Test;

public class TruncateStatementTest extends BasicValidatorTest {

    @Test
    public void validate_ok(){
        String inputText = "TRUNCATE demo.users;";
        validateOk(inputText, "validate_ok");
    }

    @Test
    public void validate_notExists_tableName(){
        String inputText = "TRUNCATE unknown_table;";
        validateFail(inputText, "validate_notExists_tableName");
    }

    @Test
    public void validate_notExists_keyspace(){
        String inputText = "TRUNCATE unknown.users;";
        validateFail(inputText, "validate_notExists_keyspace");
    }

}