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


package com.starrocks.privilege;

import com.starrocks.analysis.UserIdentity;
import com.starrocks.server.GlobalStateMgr;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AuthorizationProvider {

    /**
     * return plugin id & version
     */
    short getPluginId();
    short getPluginVersion();

    /**
     * analyze type string -> id
     */
    Set<String> getAllTypes();
    short getTypeIdByName(String typeStr) throws PrivilegeException;

    /**
     * analyze action type id -> action
     */
    Collection<Action> getAllActions(short typeId) throws PrivilegeException;
    Action getAction(short typeId, String actionName) throws PrivilegeException;

    /**
     * analyze plural type name -> type name
     */
    String getTypeNameByPlural(String plural) throws PrivilegeException;

    /**
     * generate PEntryObject by tokenlist
     */
    PEntryObject generateObject(String type, List<String> objectTokens, GlobalStateMgr mgr) throws PrivilegeException;

    PEntryObject generateUserObject(String type, UserIdentity user, GlobalStateMgr mgr) throws PrivilegeException;

    /**
     * generate PEntryObject by ON/IN ALL statements
     * e.g. GRANT SELECT ON ALL TABLES IN DATABASE db
     * grant create_table on all databases to userx
     * ==> allTypeList: ["databases"], restrictType: null, restrictName: null
     * grant select on all tables in database db1 to userx
     * ==> allTypeList: ["tables"], restrictType: database, restrictName: db1
     * grant select on all tables in all databases to userx
     * ==> allTypeList: ["tables", "databases"], restrictType: null, restrictName: null
     **/
    PEntryObject generateObject(
            String typeStr, List<String> allTypes, String restrictType, String restrictName, GlobalStateMgr mgr)
            throws PrivilegeException;

    /**
     * validate if grant is allowed
     * e.g. To forbid `NODE` privilege being granted, we should put some code here.
     */
    void validateGrant(
            String type,
            List<String> actions,
            List<PEntryObject> objects) throws PrivilegeException;

    /**
     * check if certain action of certain type is allowed on certain object.
     * Developers can implement their own logic here.
     */
    boolean check(
            short type,
            Action want,
            PEntryObject object,
            PrivilegeCollection currentPrivilegeCollection);

    /**
     * Search if any object in collection matches the specified object
     * The specified object can be fuzzy-matching object.
     * For example, `use db1` statement will pass a (db1, ALL) as the object to check if any table exists
     */
    boolean searchObject(
            short type,
            PEntryObject object,
            PrivilegeCollection currentPrivilegeCollection);

    boolean allowGrant(
            short type,
            ActionSet wants,
            List<PEntryObject> objects,
            PrivilegeCollection currentPrivilegeCollection);

    /**
     * Used for metadata upgrade
     */
    void upgradePrivilegeCollection(
            PrivilegeCollection info, short pluginId, short metaVersion) throws PrivilegeException;
}
