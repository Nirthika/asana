Product: Integration tests for WSO2 ESB asana connector

Pre-requisites:

 - Maven 3.x
 - Java 1.6 or above
 - The org.wso2.esb.integration.integration-base project is required. The test suite has been configured to download this project automatically. If the automatic download fails, download the following project and compile it using the mvn clean install command to update your local repository:
         https://github.com/wso2/esb-connectors/tree/master/integration-base-1.0.1

Tested Platform: 

 - Microsoft WINDOWS V-8.1
 - UBUNTU 13.04
 - WSO2 ESB 4.9.0

Steps to follow in setting integration test.

 1. Download ESB 4.9.0 by following the URL: http://wso2.com/products/enterprise-service-bus/

 2. Copy that zip file in to location "<ESB_CONNECTORS_HOME>/repository/".

 3. Create a Asana trial account and derive the API Key.
   i)   Using the URL "https://app.asana.com/" create a Asana trial account.
   ii)  Login to the created Asana account and go to 
		- https://asana.com/developers/documentation/getting-started/auth#register-an-app 
		- visit your Account Settings dialog
		- Developer create application
		- Give proper App Name, App URL, Redirect URL and get client Id, client Secret.
		- Get Authorization code from https://app.asana.com/-/oauth_authorize?client_id={Your client_Id}&redirect_uri={Your URI}&response_type=code&state=somerandomstate
		- Use authorization Code get the access token and refresh token from https://app.asana.com/-/oauth_token?client_id={Your client_Id}&client_secret={Your client_secret}&redirect_uri={Your URI}&code={Created authorization_code}&grant_type=authorization_code
   
 4. Update the Asana properties file at location "<ASANA_CONNECTOR_HOME>/asana-connector/asana-connector-1.0.0/org.wso2.carbon.connector/src/test/resources/artifacts/ESB/connector/config" as below.

	i)     	apiUrl			- 	The API URL specific to the domain of the created account. e.g. https://app.asana.com/api
	ii)    	apiVersion      - 	The API version of asana.
	iii)	clientId		- 	The Client ID uniquely identifies the application making the request.
	iv)		clientSecret	-	The Client Secret belonging to the app, found in the details pane of the developer console.
	v)		redirectUri		- 	The URI to redirect to on success or error. This must match the Redirect URL specified in 
								the application settings.
	vi)		grantType		-	One of authorization_code, refresh_token or password.
	vii)	refreshToken	-	If exchanging a code, a long-lived token that can be used to get new access tokens when old 
								ones expire.
	viii)	accessToken		-	The token to use in future requests against the API.
	ix)		workspace		-	The ID for a workspace.
	x)		attachment		-	The ID for an attachment.

        Note - There should be at least a difference of 10 minutes between eventOccurredAfter value and eventOccurredBefore value.   

 5. Make sure that the Asana connector is set as a module in esb-connectors parent pom.
      <module>asana/asana-connector/asana-connector-1.0.0/org.wso2.carbon.connector</module>

 6. Navigate to "<ESB_CONNECTORS_HOME>" and run the following command.
          $ mvn clean install
