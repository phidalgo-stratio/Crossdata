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

package com.stratio.meta.core.statements;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TableMetadata;
import com.stratio.meta.common.data.DeepResultSet;
import com.stratio.meta.common.result.QueryResult;
import com.stratio.meta.common.result.Result;
import com.stratio.meta.core.metadata.MetadataManager;
import com.stratio.meta.core.utils.MetaPath;
import com.stratio.meta.core.utils.MetaStep;
import com.stratio.meta.core.utils.Tree;

/**
 * Class that models a {@code DROP TABLE} statement from the META language.
 */
public class DropTableStatement extends MetaStatement {

    /**
     * Whether the keyspace has been specified in the Select statement or it should be taken from the
     * environment.
     */
    private boolean keyspaceInc = false;

    /**
     * The keyspace specified in the select statement.
     */
    private String keyspace;

    /**
     * The name of the target table.
     */
    private String tableName;

    /**
     * Whether the table should be dropped only if exists.
     */
    private boolean ifExists;

    /**
     * Class constructor.
     * @param tableName The name of the table.
     * @param ifExists Whether it should be dropped only if exists.
     */
    public DropTableStatement(String tableName, boolean ifExists) {
        if(tableName.contains(".")){
            String[] ksAndTableName = tableName.split("\\.");
            keyspace = ksAndTableName[0];
            tableName = ksAndTableName[1];
            keyspaceInc = true;
        }
        this.tableName = tableName;
        this.ifExists = ifExists;
    }

    /**
     * Get the name of the table.
     * @return The name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Set the name of the table.
     * @param tableName The name of the table.
     */
    public void setTableName(String tableName) {
        if(tableName.contains(".")){
            String[] ksAndTableName = tableName.split("\\.");
            keyspace = ksAndTableName[0];
            tableName = ksAndTableName[1];
            keyspaceInc = true;
        }
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DROP TABLE ");
        if(ifExists){
            sb.append("IF EXISTS ");
        }       
        if(keyspaceInc){
            sb.append(keyspace).append(".");
        }
        sb.append(tableName);
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Result validate(MetadataManager metadata, String targetKeyspace) {
        Result result = QueryResult.CreateSuccessQueryResult();

        String effectiveKeyspace = targetKeyspace;
        if(keyspaceInc){
            effectiveKeyspace = keyspace;
        }

        //Check that the keyspace and table exists.
        if(effectiveKeyspace == null || effectiveKeyspace.length() == 0){
            result= QueryResult.CreateFailQueryResult("Target keyspace missing or no keyspace has been selected.");
        }else{
            KeyspaceMetadata ksMetadata = metadata.getKeyspaceMetadata(effectiveKeyspace);
            if(ksMetadata == null){
                result= QueryResult.CreateFailQueryResult("Keyspace " + effectiveKeyspace + " does not exists.");
            }else {
                TableMetadata tableMetadata = metadata.getTableMetadata(effectiveKeyspace, tableName);
                if (tableMetadata == null) {
                    result= QueryResult.CreateFailQueryResult("Table " + tableName + " does not exists.");
                }
            }

        }

        return result;
    }

    @Override
    public String getSuggestion() {
        return this.getClass().toString().toUpperCase()+" EXAMPLE";
    }

    @Override
    public String translateToCQL() {
        return this.toString();
    }


    @Override
    public Statement getDriverStatement() {
        return null;
    }

    @Override
    public DeepResultSet executeDeep() {
        return new DeepResultSet();
    }

    @Override
    public Tree getPlan(MetadataManager metadataManager, String targetKeyspace) {
        Tree tree = new Tree();
        tree.setNode(new MetaStep(MetaPath.CASSANDRA, this));
        return tree;
    }
    
}