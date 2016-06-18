/*
 *  Copyright (c) 2016, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.integration.test.asana;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AsanaConnectorIntegrationTest extends ConnectorIntegrationTestBase {

    private Map<String, String> esbRequestHeadersMap = new HashMap<String, String>();
    private Map<String, String> apiRequestHeadersMap = new HashMap<String, String>();
    private Map<String, String> parametersMap = new HashMap<String, String>();

    /**
     * Set up the environment.
     */
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        init("asana-connector-1.0.0");
        esbRequestHeadersMap.put("Accept-Charset", "UTF-8");
        esbRequestHeadersMap.put("Content-Type", "application/json");
        apiRequestHeadersMap.put("Accept-Charset", "UTF-8");
        apiRequestHeadersMap.put("Content-Type", "application/json");
        String apiEndpointUrl = "https://app.asana.com/-/oauth_token?client_id=" +
                connectorProperties.getProperty("clientId") + "&client_secret=" +
                connectorProperties.getProperty("clientSecret") + "&redirect_uri=" +
                connectorProperties.getProperty("redirectUri") + "&refresh_token=" +
                connectorProperties.getProperty("refreshToken") + "&grant_type=" +
                connectorProperties.getProperty("grantType");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndpointUrl, "POST", apiRequestHeadersMap);
        final String accessToken = apiRestResponse.getBody().getString("access_token");
        connectorProperties.put("accessToken", accessToken);
        apiRequestHeadersMap.put("Authorization", "Bearer " + accessToken);
        apiRequestHeadersMap.putAll(esbRequestHeadersMap);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getSingleAttachment} integration test.")
    public void getSingleAttachmentMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getSingleAttachment");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleAttachment_mandatory.json");
        connectorProperties.setProperty("attachTaskId",
                esbRestResponse.getBody().getJSONObject("data").getJSONObject("parent").getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/attachments/" +
                connectorProperties.getProperty("attachment");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"),
                apiRestResponse.getBody().getJSONObject("data").get("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"),
                apiRestResponse.getBody().getJSONObject("data").get("created_at"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getSingleAttachment} integration test.")
    public void getSingleAttachmentNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getSingleAttachment");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleAttachment_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"getSingleAttachmentMandatory"},
            description = "asana {getAllAttachments} integration test.")
    public void getAllAttachmentsMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getAllAttachments");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllAttachments_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("attachTaskId") + "/attachments";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getAllAttachments} integration test.")
    public void getAllAttachmentsNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getAllAttachments");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllAttachments_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {createProject} integration test.")
    public void createProjectMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createProject_mandatory.json");
        connectorProperties.setProperty("projectId",
                esbRestResponse.getBody().getJSONObject("data").getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/projects/" +
                connectorProperties.getProperty("projectId");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {createProject} integration test.")
    public void createProjectOptional() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createProject_optional.json");
        connectorProperties.setProperty("projectId2",
                esbRestResponse.getBody().getJSONObject("data").getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/projects/" +
                connectorProperties.getProperty("projectId2");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"),
                apiRestResponse.getBody().getJSONObject("data").getString("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("notes"),
                apiRestResponse.getBody().getJSONObject("data").getString("notes"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {createProject} integration test.")
    public void createProjectNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createProject_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createProjectOptional"},
            description = "asana {getSingleProject} integration test.")
    public void getSingleProjectMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getSingleProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleProject_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/projects/" +
                connectorProperties.getProperty("projectId2");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"),
                apiRestResponse.getBody().getJSONObject("data").getString("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("notes"),
                apiRestResponse.getBody().getJSONObject("data").getString("notes"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getSingleProject} integration test.")
    public void getSingleProjectNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getSingleProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleProject_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"getSingleProjectMandatory"},
            description = "asana {updateProject} integration test.")
    public void updateProjectMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateProject_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/projects/" +
                connectorProperties.getProperty("projectId2");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"),
                apiRestResponse.getBody().getJSONObject("data").getString("name"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"updateProjectMandatory"},
            description = "asana {updateProject} integration test.")
    public void updateProjectOptional() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateProject_optional.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/projects/" +
                connectorProperties.getProperty("projectId2");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"),
                apiRestResponse.getBody().getJSONObject("data").getString("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("notes"),
                apiRestResponse.getBody().getJSONObject("data").getString("notes"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {updateProject} integration test.")
    public void updateProjectNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateProject_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createProjectMandatory"},
            description = "asana {deleteProject} integration test.")
    public void deleteProjectMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:deleteProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteProject_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/projects/" +
                connectorProperties.getProperty("projectId");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 404);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {deleteProject} integration test.")
    public void deleteProjectNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:deleteProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteProject_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createProjectOptional"},
            description = "asana {queryForProjects} integration test.")
    public void queryForProjectsMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:queryForProjects");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForProjects_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces/" +
                connectorProperties.getProperty("workspace") + "/projects";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {queryForProjects} integration test.")
    public void queryForProjectsNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:queryForProjects");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForProjects_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createProjectOptional"},
            description = "asana {getProjectSections} integration test.")
    public void getProjectSectionsMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getProjectSections");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectSections_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/projects/" +
                connectorProperties.getProperty("projectId2") + "/sections";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getProjectSections} integration test.")
    public void getProjectSectionsNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getProjectSections");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectSections_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {createTag} integration test.")
    public void createTagMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createTag");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createTag_mandatory.json");
        connectorProperties.setProperty("tagId", esbRestResponse.getBody().getJSONObject("data").getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tags/" +
                connectorProperties.getProperty("tagId");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"),
                apiRestResponse.getBody().getJSONObject("data").get("created_at"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTagMandatory"},
            description = "asana {createTag} integration test.")
    public void createTagOptional() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createTag");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createTag_optional.json");
        connectorProperties.setProperty("tagId2", esbRestResponse.getBody().getJSONObject("data").getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tags/" +
                connectorProperties.getProperty("tagId2");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"),
                apiRestResponse.getBody().getJSONObject("data").get("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"),
                apiRestResponse.getBody().getJSONObject("data").get("created_at"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTagOptional"},
            description = "asana {createTag} integration test.")
    public void createTagNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createTag");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createTag_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"},dependsOnMethods = {"createTagNegative"},
            description = "asana {getSingleTag} integration test.")
    public void getSingleTagMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getSingleTag");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleTag_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tags/" +
                connectorProperties.getProperty("tagId2");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"),
                apiRestResponse.getBody().getJSONObject("data").get("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"),
                apiRestResponse.getBody().getJSONObject("data").get("created_at"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getSingleTag} integration test.")
    public void getSingleTagNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getSingleTag");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleTag_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"getSingleTagNegative"},
            description = "asana {updateTag} integration test.")
    public void updateTagMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateTag");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateTag_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tags/" +
                connectorProperties.getProperty("tagId2");
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"),
                apiRestResponse.getBody().getJSONObject("data").get("name"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"updateTagMandatory"},
            description = "asana {updateTag} integration test.")
    public void updateTagNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateTag");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateTag_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"updateTagNegative"},
            description = "asana {queryForTags} integration test.")
    public void queryForTagsMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:queryForTags");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTags_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces/" +
                connectorProperties.getProperty("workspace") + "/tags";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"queryForTagsMandatory"},
            description = "asana {queryForTags} integration test.")
    public void queryForTagsNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:queryForTags");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTags_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"queryForTagsNegative"},
            description = "asana {createTask} integration test.")
    public void createTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createTask_mandatory.json");
        connectorProperties.setProperty("taskId",
                esbRestResponse.getBody().getJSONObject("data").getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"),
                apiRestResponse.getBody().getJSONObject("data").get("created_at"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskMandatory"},
            description = "asana {createTask} integration test.")
    public void createTaskOptional() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createTask_optional.json");
        connectorProperties.setProperty("taskId2",
                esbRestResponse.getBody().getJSONObject("data").getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId2");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"),
                apiRestResponse.getBody().getJSONObject("data").get("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("notes"),
                apiRestResponse.getBody().getJSONObject("data").get("notes"));
        connectorProperties.setProperty("createdAt",
                esbRestResponse.getBody().getJSONObject("data").getString("created_at"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {createTask} integration test.")
    public void createTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskOptional"},
            description = "asana {getTask} integration test.")
    public void getTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTask_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"),
                apiRestResponse.getBody().getJSONObject("data").get("created_at"));

    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getTask} integration test.")
    public void getTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"getTaskMandatory"},
            description = "asana {updateTask} integration test.")
    public void updateTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateTask_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getJSONObject("assignee").get("id"),
                apiRestResponse.getBody().getJSONObject("data").getJSONObject("assignee").get("id"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"updateTaskMandatory"},
            description = "asana {updateTask} integration test.")
    public void updateTaskOptional() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateTask_optional.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getJSONObject("assignee").get("id"),
                apiRestResponse.getBody().getJSONObject("data").getJSONObject("assignee").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"),
                apiRestResponse.getBody().getJSONObject("data").get("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("notes"),
                apiRestResponse.getBody().getJSONObject("data").get("notes"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"updateTaskOptional"},
            description = "asana {updateTask} integration test.")
    public void updateTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskOptional"},
            description = "asana {queryForTasks} integration test.")
    public void queryForTasksMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:queryForTasks");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTasks_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks?workspace=" +
                connectorProperties.getProperty("workspace") + "&assignee=nirthika.rajendran@gmail.com";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"queryForTasksMandatory"},
            description = "asana {queryForTasks} integration test.")
    public void queryForTasksOptional() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:queryForTasks");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTasks_optional.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks?workspace=" +
                connectorProperties.getProperty("workspace") + "&assignee=nirthika.rajendran@gmail.com" +
                "&modified_since=" + connectorProperties.getProperty("createdAt") +
                "&completed_since=" + connectorProperties.getProperty("createdAt");
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {queryForTasks} integration test.")
    public void queryForTasksNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:queryForTasks");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTasks_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskOptional"},
            description = "asana {createSubtask} integration test.")
    public void createSubtaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createSubtask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createSubtask_mandatory.json");
        connectorProperties.setProperty("subtaskId", esbRestResponse.getBody().getJSONObject("data").getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("subtaskId");
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"),
                apiRestResponse.getBody().getJSONObject("data").get("created_at"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createSubtaskMandatory"},
            description = "asana {createSubtask} integration test.")
    public void createSubtaskOptional() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createSubtask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createSubtask_optional.json");
        connectorProperties.setProperty("subtaskId2", esbRestResponse.getBody().getJSONObject("data").getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("subtaskId2");
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"),
                apiRestResponse.getBody().getJSONObject("data").get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"),
                apiRestResponse.getBody().getJSONObject("data").get("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("notes"),
                apiRestResponse.getBody().getJSONObject("data").get("notes"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {createSubtask} integration test.")
    public void createSubtaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createSubtask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createSubtask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createSubtaskOptional"},
            description = "asana {getAllSubtasks} integration test.")
    public void getAllSubtasksMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getAllSubtasks");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllSubtasks_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId2") + "/subtasks";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getAllSubtasks} integration test.")
    public void getAllSubtasksNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getAllSubtasks");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllSubtasks_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"updateTaskNegative"},
            description = "asana {changeParentOfTask} integration test.")
    public void changeParentOfTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:changeParentOfTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_changeParentOfTask_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getJSONObject("parent").get("name"),
                apiRestResponse.getBody().getJSONObject("data").getJSONObject("parent").get("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getJSONObject("parent").get("id"),
                apiRestResponse.getBody().getJSONObject("data").getJSONObject("parent").get("id"));
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"changeParentOfTaskMandatory"},
            description = "asana {changeParentOfTask} integration test.")
    public void changeParentOfTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:changeParentOfTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_changeParentOfTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"changeParentOfTaskNegative"},
            description = "asana {deleteTask} integration test.")
    public void deleteTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:deleteTask");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteTask_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/tasks/" + connectorProperties.getProperty("taskId");
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 404);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {deleteTask} integration test.")
    public void deleteTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:deleteTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskOptional"},
            description = "asana {createCommentToTask} integration test.")
    public void createCommentToTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createCommentToTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createCommentToTask_mandatory.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {createCommentToTask} integration test.")
    public void createCommentToTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createCommentToTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createCommentToTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskOptional"},
            description = "asana {getAllStoriesOfTask} integration test.")
    public void getAllStoriesOfTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getAllStoriesOfTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllStoriesOfTask_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId2") + "/stories";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("created_at"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("created_at"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("type"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("type"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getAllStoriesOfTask} integration test.")
    public void getAllStoriesOfTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getAllStoriesOfTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllStoriesOfTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskOptional"},
            description = "asana {createFollowersToTask} integration test.")
    public void createFollowersToTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createFollowersToTask");
        String methodName = "createFollowersToTask";
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createFollowersToTask_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId2");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength =
                esbRestResponse.getBody().getJSONObject("data").getJSONArray("followers").length();
        final int apiResponseArrayLength =
                apiRestResponse.getBody().getJSONObject("data").getJSONArray("followers").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {createFollowersToTask} integration test.")
    public void createFollowersToTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createFollowersToTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createFollowersToTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createFollowersToTaskMandatory"},
            description = "asana {removeFollowersFromTask} integration test.")
    public void removeFollowersFromTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:removeFollowersFromTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeFollowersFromTask_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId2");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength =
                esbRestResponse.getBody().getJSONObject("data").getJSONArray("followers").length();
        final int apiResponseArrayLength =
                apiRestResponse.getBody().getJSONObject("data").getJSONArray("followers").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {removeFollowersFromTask} integration test.")
    public void removeFollowersFromTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:removeFollowersFromTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeFollowersFromTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskOptional", "createProjectOptional"},
            description = "asana {addTaskToProject} integration test.")
    public void addTaskToProjectMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:addTaskToProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_addTaskToProject_mandatory.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {addTaskToProject} integration test.")
    public void addTaskToProjectNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:addTaskToProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_addTaskToProject_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"addTaskToProjectMandatory"},
            description = "asana {getProjectTasks} integration test.")
    public void getProjectTasksMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getProjectTasks");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectTasks_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/projects/" +
                connectorProperties.getProperty("projectId2") + "/tasks";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getProjectTasks} integration test.")
    public void getProjectTasksNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getProjectTasks");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectTasks_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"getProjectTasksMandatory"},
            description = "asana {getProjectDetailsOfTask} integration test.")
    public void getProjectDetailsOfTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getProjectDetailsOfTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectDetailsOfTask_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId2") + "/projects";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getProjectDetailsOfTask} integration test.")
    public void getProjectDetailsOfTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getProjectDetailsOfTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectDetailsOfTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"addTaskToProjectMandatory"},
            description = "asana {removeTaskFromProject} integration test.")
    public void removeTaskFromProjectMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:removeTaskFromProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeTaskFromProject_mandatory.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {removeTaskFromProject} integration test.")
    public void removeTheTaskFromProjectNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:removeTaskFromProject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeTaskFromProject_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }


    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskOptional"},
            description = "asana {commentOnObject} integration test.")
    public void commentOnObjectMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:commentOnObject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_commentOnObject_mandatory.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {commentOnObject} integration test.")
    public void commentOnObjectNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:commentOnObject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_commentOnObject_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"commentOnObjectMandatory"},
            description = "asana {getStoriesOnObject} integration test.")
    public void getStoriesOnObjectMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getStoriesOnObject");
        String methodName = "getStoriesOnObject";
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getStoriesOnObject_mandatory.json");
        connectorProperties.setProperty("storyId",
                esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId2") + "/stories";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("text"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("text"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getStoriesOnObject} integration test.")
    public void getStoriesOnObjectNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getStoriesOnObject");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getStoriesOnObject_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"getStoriesOnObjectMandatory"},
            description = "asana {getSingleStory} integration test.")
    public void getSingleStoryMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getSingleStory");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleStory_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/stories/" +
                connectorProperties.getProperty("storyId");
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("id"),
                apiRestResponse.getBody().getJSONObject("data").getString("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("text"),
                apiRestResponse.getBody().getJSONObject("data").getString("text"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("created_at"),
                apiRestResponse.getBody().getJSONObject("data").getString("created_at"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getSingleStory} integration test.")
    public void getSingleStoryNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getSingleStory");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleStory_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTaskOptional",},
            description = "asana {createTagToTask} integration test.")
    public void createTagToTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createTagToTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createTagToTask_mandatory.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {createTagToTask} integration test.")
    public void createTagToTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createTagToTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createTagToTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"createTagToTaskMandatory"},
            description = "asana {getTagsOfTask} integration test.")
    public void getTagsOfTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getTagsOfTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTagsOfTask_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tasks/" +
                connectorProperties.getProperty("taskId2") + "/tags";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getTagsOfTask} integration test.")
    public void getTagsOfTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getTagsOfTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTagsOfTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"getTagsOfTaskMandatory"},
            description = "asana {getTasksWithTag} integration test.")
    public void getTasksWithTagMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getTasksWithTag");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTasksWithTag_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/tags/" +
                connectorProperties.getProperty("tagId2") + "/tasks";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getTasksWithTag} integration test.")
    public void getTasksWithTagNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getTasksWithTag");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTasksWithTag_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"getTasksWithTagMandatory"},
            description = "asana {removeTagFromTask} integration test.")
    public void removeTagFromTaskMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:removeTagFromTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeTagFromTask_mandatory.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"removeTagFromTaskMandatory"},
            description = "asana {removeTagFromTask} integration test.")
    public void removeTagFromTaskNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:removeTagFromTask");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeTagFromTask_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {query} integration test.")
    public void queryMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:query");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_query_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces/" +
                connectorProperties.getProperty("workspace") + "/typeahead?type=task&query=tas";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {query} integration test.")
    public void queryOptional() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:query");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_query_optional.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces/" +
                connectorProperties.getProperty("workspace") + "/typeahead?type=task&query=tas&count=10";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {query} integration test.")
    public void queryNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:query");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_query_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getAllWorkspaces} integration test.")
    public void getAllWorkspacesMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getAllWorkspaces");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllWorkspaces_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getWorkspace} integration test.")
    public void getWorkspaceMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getWorkspace");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getWorkspace_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces/" +
                connectorProperties.getProperty("workspace");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("id"),
                apiRestResponse.getBody().getJSONObject("data").getString("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"),
                apiRestResponse.getBody().getJSONObject("data").getString("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getWorkspace} integration test.")
    public void getWorkspaceNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getWorkspace");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getWorkspace_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {updateWorkspace} integration test.")
    public void updateWorkspaceMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateWorkspace");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateWorkspace_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces/" +
                connectorProperties.getProperty("workspace");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("id"),
                apiRestResponse.getBody().getJSONObject("data").getString("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"),
                apiRestResponse.getBody().getJSONObject("data").getString("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {updateWorkspace} integration test.")
    public void updateWorkspaceNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:updateWorkspace");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateWorkspace_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {addUser} integration test.")
    public void addUserMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:addUser");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_addUser_mandatory.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("email"), "dayasha1006@gmail.com");
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {addUser} integration test.")
    public void addUserNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:addUser");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_addUser_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {typeaheadSearch} integration test.")
    public void typeaheadSearchMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:typeaheadSearch");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_typeaheadSearch_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces/" +
                connectorProperties.getProperty("workspace") + "/typeahead?type=task&query=tas";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {typeaheadSearch} integration test.")
    public void typeaheadSearchOptional() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:typeaheadSearch");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_typeaheadSearch_optional.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces/" +
                connectorProperties.getProperty("workspace") + "/typeahead?type=task&query=tas&count=10";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {typeaheadSearch} integration test.")
    public void typeaheadSearchNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:typeaheadSearch");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_typeaheadSearch_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getAllUsers} integration test.")
    public void getAllUsersMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getAllUsers");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllUsers_mandatory.json");
        connectorProperties.setProperty("userId",
                esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/workspaces/" +
                connectorProperties.getProperty("workspace") + "/users";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
        final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
        Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"),
                apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getAllUsers} integration test.")
    public void getAllUsersNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getAllUsers");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllUsers_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }

    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"getAllUsersMandatory"},
            description = "asana {getUser} integration test.")
    public void getUserMandatory() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getUser");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getUser_mandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/" +
                connectorProperties.getProperty("apiVersion") + "/users/" +
                connectorProperties.getProperty("userId");
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("id"),
                apiRestResponse.getBody().getJSONObject("data").getString("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("email"),
                apiRestResponse.getBody().getJSONObject("data").getString("email"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"),
                apiRestResponse.getBody().getJSONObject("data").getString("name"));
    }

    @Test(groups = {"wso2.esb"},
            description = "asana {getUser} integration test.")
    public void getUserNegative() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getUser");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getUser_negative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
    }
}