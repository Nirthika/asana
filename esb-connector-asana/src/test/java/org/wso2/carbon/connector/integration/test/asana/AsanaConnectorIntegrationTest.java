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

import java.io.IOException;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;

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
		
		String apiEndpointUrl = "https://app.asana.com/-/oauth_token?client_id=" + connectorProperties.getProperty("clientId") +
                "&client_secret=" + connectorProperties.getProperty("clientSecret") +"&redirect_uri="+connectorProperties.getProperty("redirectUri")+
				"&refresh_token=" + connectorProperties.getProperty("refreshToken")+"&grant_type="+connectorProperties.getProperty("grantType") ;

        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndpointUrl, "POST", apiRequestHeadersMap);
        final String accessToken = apiRestResponse.getBody().getString("access_token");
        connectorProperties.put("accessToken", accessToken);
        apiRequestHeadersMap.put("Authorization", "Bearer " + accessToken);
        apiRequestHeadersMap.putAll(esbRequestHeadersMap);
	  
       /*String accessToken = connectorProperties.getProperty("accessToken");
       apiRequestHeadersMap.put("Authorization", "Bearer " + accessToken);*/

    }
	
	@Test(groups = { "wso2.esb" },description = "asana {getSingleAttachment} integration test.")
	public void getSingleAttachmentMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getSingleAttachment");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleAttachment_mandatory.json");
		connectorProperties.setProperty("attachTaskId", esbRestResponse.getBody().getJSONObject("data").getJSONObject("parent").getString("id"));
		System.out.println("000000000000000000000000000");
		System.out.println(connectorProperties.getProperty("attachTaskId"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion")+"/attachments/"+connectorProperties.getProperty("attachment");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"), apiRestResponse.getBody().getJSONObject("data").get("name"));		
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"), apiRestResponse.getBody().getJSONObject("data").get("created_at"));		
	}

	@Test(groups = { "wso2.esb" },description = "asana {getSingleAttachment} integration test.")
	public void getSingleAttachmentNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getSingleAttachment");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleAttachment_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "getSingleAttachmentMandatory" } ,description = "asana {getAllAttachments} integration test.")
	public void getAllAttachmentsMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllAttachments");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllAttachments_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("attachTaskId")+"/attachments";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"));
	}

	@Test(groups = { "wso2.esb" },description = "asana {getAllAttachments} integration test.")
	public void getAllAttachmentsNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllAttachments");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllAttachments_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {createAProject} integration test.")
    public void createAProjectMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createAProject");	
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createAProject_mandatory.json");
		connectorProperties.setProperty("projectId", esbRestResponse.getBody().getJSONObject("data").getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/projects/"+connectorProperties.getProperty("projectId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
	}

	@Test(groups = { "wso2.esb" }, description = "asana {createAProject} integration test.")
    public void createAProjectOptional() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createAProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createAProject_optional.json");
		connectorProperties.setProperty("projectId2", esbRestResponse.getBody().getJSONObject("data").getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/projects/"+connectorProperties.getProperty("projectId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"), apiRestResponse.getBody().getJSONObject("data").getString("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("notes"),apiRestResponse.getBody().getJSONObject("data").getString("notes"));
	}

	@Test(groups = { "wso2.esb" }, description = "asana {createAProject} integration test.")
    public void createAProjectNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createAProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createAProject_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createAProjectOptional" } ,description = "asana {getSingleProject} integration test.")
	public void getSingleProjectMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getSingleProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleProject_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/projects/"+connectorProperties.getProperty("projectId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"), apiRestResponse.getBody().getJSONObject("data").getString("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("notes"),apiRestResponse.getBody().getJSONObject("data").getString("notes"));
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {getSingleProject} integration test.")
	public void getSingleProjectNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getSingleProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getSingleProject_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "getSingleProjectMandatory" }, description = "asana {updateAProject} integration test.")
    public void updateAProjectMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateAProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateAProject_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/projects/"+connectorProperties.getProperty("projectId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"), apiRestResponse.getBody().getJSONObject("data").getString("name"));
	}

	@Test(groups = { "wso2.esb" },dependsOnMethods = { "updateAProjectMandatory" }, description = "asana {updateAProject} integration test.")
    public void updateAProjectOptional() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateAProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateAProject_optional.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/projects/"+connectorProperties.getProperty("projectId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"), apiRestResponse.getBody().getJSONObject("data").getString("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("notes"),apiRestResponse.getBody().getJSONObject("data").getString("notes"));
	}

	@Test(groups = { "wso2.esb" }, description = "asana {updateAProject} integration test.")
    public void updateAProjectNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateAProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateAProject_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createAProjectMandatory" }, description = "asana {deleteAProject} integration test.")
	public void deleteAProjectMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteAProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteAProject_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/projects/"+connectorProperties.getProperty("projectId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 404);
	}	
	
	@Test(groups = { "wso2.esb" }, description = "asana {deleteAProject} integration test.")
	public void deleteAProjectNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteAProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteAProject_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}

	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createAProjectOptional" } ,description = "asana {queryForProjects} integration test.")
	public void queryForProjectsMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:queryForProjects");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForProjects_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/workspaces/"+connectorProperties.getProperty("workspace")+"/projects";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {queryForProjects} integration test.")
	public void queryForProjectsNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:queryForProjects");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForProjects_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createAProjectOptional" } ,description = "asana {getProjectSections} integration test.")
	public void getProjectSectionsMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getProjectSections");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectSections_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/projects/"+connectorProperties.getProperty("projectId2")+"/sections";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		//Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		//Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {getProjectSections} integration test.")
	public void getProjectSectionsNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getProjectSections");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectSections_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	@Test(groups = { "wso2.esb" }, description = "asana {createATag} integration test.")
    public void createATagMandatory() throws IOException, JSONException {
    	esbRequestHeadersMap.put("Action", "urn:createATag");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createATag_mandatory.json");
		connectorProperties.setProperty("tagId", esbRestResponse.getBody().getJSONObject("data").getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tags/"+connectorProperties.getProperty("tagId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"), apiRestResponse.getBody().getJSONObject("data").get("created_at"));
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATagMandatory" }, description = "asana {createATag} integration test.")
    public void createATagOptional() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createATag");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createATag_optional.json");
		connectorProperties.setProperty("tagId2", esbRestResponse.getBody().getJSONObject("data").getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tags/"+connectorProperties.getProperty("tagId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"), apiRestResponse.getBody().getJSONObject("data").get("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"), apiRestResponse.getBody().getJSONObject("data").get("created_at"));
	}
	
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "createATagOptional" } ,description = "asana {createATag} integration test.")
    public void createATagNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createATag");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createATag_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}	
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATagNegative" } ,description = "asana {getASingleTag} integration test.")
	public void getASingleTagMandatory() throws IOException, JSONException {
    	esbRequestHeadersMap.put("Action", "urn:getASingleTag");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getASingleTag_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tags/"+connectorProperties.getProperty("tagId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"), apiRestResponse.getBody().getJSONObject("data").get("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"), apiRestResponse.getBody().getJSONObject("data").get("created_at"));
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {getASingleTag} integration test.")
	public void getASingleTagNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getASingleTag");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getASingleTag_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}	
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "getASingleTagNegative" } ,description = "asana {updateATag} integration test.")
	public void updateATagMandatory() throws IOException, JSONException {
    	esbRequestHeadersMap.put("Action", "urn:updateATag");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateATag_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tags/"+connectorProperties.getProperty("tagId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"), apiRestResponse.getBody().getJSONObject("data").get("name"));
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "updateATagMandatory" } ,description = "asana {updateATag} integration test.")
	public void updateATagNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateATag");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateATag_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "updateATagNegative" } ,description = "asana {queryForTags} integration test.")
	public void queryForTagsMandatory() throws IOException, JSONException {
    	esbRequestHeadersMap.put("Action", "urn:queryForTags");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTags_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/workspaces/"+connectorProperties.getProperty("workspace")+"/tags";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "queryForTagsMandatory" },description = "asana {queryForTags} integration test.")
	public void queryForTagsNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:queryForTags");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTags_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "queryForTagsNegative" }, description = "asana {createATask} integration test.")
    public void createATaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createATask_mandatory.json");
		connectorProperties.setProperty("taskId", esbRestResponse.getBody().getJSONObject("data").getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"), apiRestResponse.getBody().getJSONObject("data").get("created_at"));
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATaskMandatory" }, description = "asana {createATask} integration test.")
	public void createATaskOptional() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createATask_optional.json");
		connectorProperties.setProperty("taskId2", esbRestResponse.getBody().getJSONObject("data").getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"), apiRestResponse.getBody().getJSONObject("data").get("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("notes"), apiRestResponse.getBody().getJSONObject("data").get("notes"));
		connectorProperties.setProperty("createdAt", esbRestResponse.getBody().getJSONObject("data").getString("created_at"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {createATask} integration test.")
    public void createATaskNegative() throws IOException, JSONException {
    	esbRequestHeadersMap.put("Action", "urn:createATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createATask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATaskOptional" } ,description = "asana {getATask} integration test.")
	public void getATaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getATask_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"), apiRestResponse.getBody().getJSONObject("data").get("created_at"));
	
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {getATask} integration test.")
	public void getATaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getATask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "getATaskMandatory" }, description = "asana {updateATask} integration test.")
	public void updateATaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateATask_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getJSONObject("assignee").get("id"), apiRestResponse.getBody().getJSONObject("data").getJSONObject("assignee").get("id"));	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "updateATaskMandatory" }, description = "asana {updateATask} integration test.")
	public void updateATaskOptional() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateATask_optional.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getJSONObject("assignee").get("id"), apiRestResponse.getBody().getJSONObject("data").getJSONObject("assignee").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"), apiRestResponse.getBody().getJSONObject("data").get("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("notes"), apiRestResponse.getBody().getJSONObject("data").get("notes"));	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "updateATaskOptional" }, description = "asana {updateATask} integration test.")
	public void updateATaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateATask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATaskOptional" }, description = "asana {queryForTasks} integration test.")
	public void queryForTasksMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:queryForTasks");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTasks_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks?workspace="+connectorProperties.getProperty("workspace")+"&assignee=nirthika.rajendran@gmail.com";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));		
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "queryForTasksMandatory" }, description = "asana {queryForTasks} integration test.")
	public void queryForTasksOptional() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:queryForTasks");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTasks_optional.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks?workspace="+connectorProperties.getProperty("workspace")+"&assignee=nirthika.rajendran@gmail.com"+"&modified_since="+connectorProperties.getProperty("createdAt")+"&completed_since="+connectorProperties.getProperty("createdAt");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));	
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {queryForTasks} integration test.")
	public void queryForTasksNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:queryForTasks");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_queryForTasks_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATaskOptional" }, description = "asana {createASubtask} integration test.")
    public void createASubtaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createASubtask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createASubtask_mandatory.json");
		connectorProperties.setProperty("subtaskId", esbRestResponse.getBody().getJSONObject("data").getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("subtaskId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("created_at"), apiRestResponse.getBody().getJSONObject("data").get("created_at"));
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createASubtaskMandatory" }, description = "asana {createASubtask} integration test.")
    public void createASubtaskOptional() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createASubtask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createASubtask_optional.json");
		connectorProperties.setProperty("subtaskId2", esbRestResponse.getBody().getJSONObject("data").getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("subtaskId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("id"), apiRestResponse.getBody().getJSONObject("data").get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("name"), apiRestResponse.getBody().getJSONObject("data").get("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").get("notes"), apiRestResponse.getBody().getJSONObject("data").get("notes"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {createASubtask} integration test.")
    public void createASubtaskNegative() throws IOException, JSONException {
    	esbRequestHeadersMap.put("Action", "urn:createASubtask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createASubtask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createASubtaskOptional" } ,description = "asana {getAllSubtasks} integration test.")
	public void getAllSubtasksMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllSubtasks");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllSubtasks_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId2")+"/subtasks";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"));
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {getAllSubtasks} integration test.")
	public void getAllSubtasksNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllSubtasks");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllSubtasks_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "updateATaskNegative" }, description = "asana {changeParentOfTask} integration test.")
    public void changeParentOfTaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:changeParentOfTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_changeParentOfTask_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getJSONObject("parent").get("name"), apiRestResponse.getBody().getJSONObject("data").getJSONObject("parent").get("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getJSONObject("parent").get("id"), apiRestResponse.getBody().getJSONObject("data").getJSONObject("parent").get("id"));
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "changeParentOfTaskMandatory" }, description = "asana {changeParentOfTask} integration test.")
    public void changeParentOfTaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:changeParentOfTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_changeParentOfTask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}	
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "changeParentOfTaskNegative" }, description = "asana {deleteATask} integration test.")
	public void deleteATaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteATask_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 404);
	}	
	
	@Test(groups = { "wso2.esb" }, description = "asana {deleteATask} integration test.")
	public void deleteATaskNegative() throws IOException, JSONException {
    	esbRequestHeadersMap.put("Action", "urn:deleteATask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteATask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}	
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATaskOptional" }, description = "asana {createACommentToTask} integration test.")
    public void createACommentToTaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createACommentToTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createACommentToTask_mandatory.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {createACommentToTask} integration test.")
	public void createACommentToTaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createACommentToTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createACommentToTask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATaskOptional" } ,description = "asana {getAllStoriesOfTask} integration test.")
	public void getAllStoriesOfTaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllStoriesOfTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllStoriesOfTask_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId2")+"/stories";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("created_at"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("created_at"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("type"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("type"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {getAllStoriesOfTask} integration test.")
	public void getAllStoriesOfTaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllStoriesOfTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllStoriesOfTask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATaskOptional" } ,description = "asana {createFollowersToTask} integration test.")
	public void createFollowersToTaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createFollowersToTask");
		
		String methodName = "createFollowersToTask";
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createFollowersToTask_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONObject("data").getJSONArray("followers").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONObject("data").getJSONArray("followers").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {createFollowersToTask} integration test.")
	public void createFollowersToTaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createFollowersToTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createFollowersToTask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createFollowersToTaskMandatory" } ,description = "asana {removeFollowersFromTask} integration test.")
	public void removeFollowersFromTaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:removeFollowersFromTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeFollowersFromTask_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId2");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONObject("data").getJSONArray("followers").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONObject("data").getJSONArray("followers").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {removeFollowersFromTask} integration test.")
	public void removeFollowersFromTaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:removeFollowersFromTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeFollowersFromTask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}	
	
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "createATaskOptional","createAProjectOptional" },description = "asana {addATaskToProject} integration test.")
    public void addATaskToProjectMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:addATaskToProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_addATaskToProject_mandatory.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {addATaskToProject} integration test.")
	public void addATaskToProjectNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:addATaskToProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_addATaskToProject_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "addATaskToProjectMandatory" } ,description = "asana {getProjectTasks} integration test.")
	public void getProjectTasksMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getProjectTasks");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectTasks_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/projects/"+connectorProperties.getProperty("projectId2")+"/tasks";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {getProjectTasks} integration test.")
	public void getProjectTasksNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getProjectTasks");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectTasks_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "getProjectTasksMandatory" } ,description = "asana {getProjectDetailsOfTask} integration test.")
	public void getProjectDetailsOfTaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getProjectDetailsOfTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectDetailsOfTask_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId2")+"/projects";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("name"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {getProjectDetailsOfTask} integration test.")
	public void getProjectDetailsOfTaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getProjectDetailsOfTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getProjectDetailsOfTask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}	
	
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "addATaskToProjectMandatory" },description = "asana {removeATaskFromProject} integration test.")
    public void removeATaskFromProjectMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:removeATaskFromProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeATaskFromProject_mandatory.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {removeATaskFromProject} integration test.")
	public void removeTheTaskFromProjectNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:removeATaskFromProject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeATaskFromProject_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATaskOptional" }, description = "asana {commentingOnAnObject} integration test.")
    public void commentingOnAnObjectMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:commentingOnAnObject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_commentingOnAnObject_mandatory.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {commentingOnAnObject} integration test.")
	public void commentingOnAnObjectNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:commentingOnAnObject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_commentingOnAnObject_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "commentingOnAnObjectMandatory" } ,description = "asana {getStoriesOnObject} integration test.")
	public void getStoriesOnObjectMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getStoriesOnObject");
		String methodName = "getStoriesOnObject";
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getStoriesOnObject_mandatory.json");
		connectorProperties.setProperty("storyId", esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId2")+"/stories";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("text"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("text"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {getStoriesOnObject} integration test.")
	public void getStoriesOnObjectNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getStoriesOnObject");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getStoriesOnObject_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "getStoriesOnObjectMandatory" } ,description = "asana {getASingleStory} integration test.")
	public void getASingleStoryMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getASingleStory");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getASingleStory_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/stories/"+connectorProperties.getProperty("storyId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("id"), apiRestResponse.getBody().getJSONObject("data").getString("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("text"), apiRestResponse.getBody().getJSONObject("data").getString("text"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("created_at"), apiRestResponse.getBody().getJSONObject("data").getString("created_at"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {getASingleStory} integration test.")
	public void getASingleStoryNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getASingleStory");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getASingleStory_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	
	}
	
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "createATaskOptional",},description = "asana {createATagToTask} integration test.")
    public void createATagToTaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createATagToTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createATagToTask_mandatory.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {createATagToTask} integration test.")
	public void createATagToTaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createATagToTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createATagToTask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "createATagToTaskMandatory" } ,description = "asana {getTagsOfTask} integration test.")
	public void getTagsOfTaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getTagsOfTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTagsOfTask_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tasks/"+connectorProperties.getProperty("taskId2")+"/tags";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {getTagsOfTask} integration test.")
	public void getTagsOfTaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getTagsOfTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTagsOfTask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "getTagsOfTaskMandatory" } ,description = "asana {getTasksWithTag} integration test.")
	public void getTasksWithTagMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getTasksWithTag");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTasksWithTag_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/tags/"+connectorProperties.getProperty("tagId2")+"/tasks";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {getTasksWithTag} integration test.")
	public void getTasksWithTagNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getTasksWithTag");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getTasksWithTag_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}	
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "getTasksWithTagMandatory"} ,description = "asana {removeATagFromTask} integration test.")
	public void removeATagFromTaskMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:removeATagFromTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeATagFromTask_mandatory.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "removeATagFromTaskMandatory" },description = "asana {removeATagFromTask} integration test.")
	public void removeATagFromTaskNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:removeATagFromTask");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_removeATagFromTask_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {query} integration test.")
	public void queryMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:query");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_query_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion")+"/workspaces/"+connectorProperties.getProperty("workspace") +"/typeahead?type=task&query=tas";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
	
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {query} integration test.")
	public void queryOptional() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:query");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_query_optional.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion")+"/workspaces/"+connectorProperties.getProperty("workspace") +"/typeahead?type=task&query=tas&count=10";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));	
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {query} integration test.")
	public void queryNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:query");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_query_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
	
	@Test(groups = { "wso2.esb" },description = "asana {getAllWorkspaces} integration test.")
	public void getAllWorkspacesMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllWorkspaces");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllWorkspaces_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/workspaces";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
	}
	
	@Test(groups = { "wso2.esb" } ,description = "asana {getAWorkspace} integration test.")
	public void getAWorkspaceMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAWorkspace");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAWorkspace_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/workspaces/"+connectorProperties.getProperty("workspace");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("id"), apiRestResponse.getBody().getJSONObject("data").getString("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"), apiRestResponse.getBody().getJSONObject("data").getString("name"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {getAWorkspace} integration test.")
	public void getAWorkspaceNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAWorkspace");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAWorkspace_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" } ,description = "asana {updateAWorkspace} integration test.")
	public void updateAWorkspaceMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateAWorkspace");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateAWorkspace_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/workspaces/"+connectorProperties.getProperty("workspace");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("id"), apiRestResponse.getBody().getJSONObject("data").getString("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"), apiRestResponse.getBody().getJSONObject("data").getString("name"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {updateAWorkspace} integration test.")
	public void updateAWorkspaceNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateAWorkspace");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateAWorkspace_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" } ,description = "asana {addUser} integration test.")
	public void addUserMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:addUser");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_addUser_mandatory.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("email"), "dayasha1006@gmail.com");
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {addUser} integration test.")
	public void addUserNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:addUser");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_addUser_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);	
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {typeaheadSearch} integration test.")
	public void typeaheadSearchMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:typeaheadSearch");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_typeaheadSearch_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/workspaces/"+connectorProperties.getProperty("workspace")+"/typeahead?type=task&query=tas";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));	
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {typeaheadSearch} integration test.")
	public void typeaheadSearchOptional() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:typeaheadSearch");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_typeaheadSearch_optional.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion")+"/workspaces/"+connectorProperties.getProperty("workspace") +"/typeahead?type=task&query=tas&count=10";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));	
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {typeaheadSearch} integration test.")
	public void typeaheadSearchNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:typeaheadSearch");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_typeaheadSearch_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	@Test(groups = { "wso2.esb" } ,description = "asana {getAllUsers} integration test.")
	public void getAllUsersMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllUsers");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllUsers_mandatory.json");
		connectorProperties.setProperty("userId", esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).getString("id"));
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/workspaces/"+connectorProperties.getProperty("workspace")+"/users";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		final int esbResponseArrayLength = esbRestResponse.getBody().getJSONArray("data").length();
		final int apiResponseArrayLength = apiRestResponse.getBody().getJSONArray("data").length();
		Assert.assertEquals(esbResponseArrayLength, apiResponseArrayLength);
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"), apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {getAllUsers} integration test.")
	public void getAllUsersNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllUsers");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAllUsers_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
	
	@Test(groups = { "wso2.esb" },dependsOnMethods = { "getAllUsersMandatory" } ,description = "asana {getASingleUser} integration test.")
	public void getASingleUserMandatory() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getASingleUser");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getASingleUser_mandatory.json");
		String apiEndPoint = connectorProperties.getProperty("apiUrl")+"/"+connectorProperties.getProperty("apiVersion") +"/users/"+connectorProperties.getProperty("userId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("id"), apiRestResponse.getBody().getJSONObject("data").getString("id"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("email"), apiRestResponse.getBody().getJSONObject("data").getString("email"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("data").getString("name"), apiRestResponse.getBody().getJSONObject("data").getString("name"));
	}
	
	@Test(groups = { "wso2.esb" }, description = "asana {getASingleUser} integration test.")
	public void getASingleUserNegative() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getASingleUser");
		RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getASingleUser_negative.json");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);	
	}
}