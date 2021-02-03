import groovy.json.JsonSlurper
pipeline{
	agent any 
		stages{
			stage('Login'){
				steps{

								println 'LOGIN IN ANYPOINT PLATFORM'
		
	def loginRequest =   "curl -H 'Content-Type: application/x-www-form-urlencoded' -X POST -d username=techuser -d password=Avantmoney1 https://eu1.anypoint.mulesoft.com/accounts/login".execute().text

		def slurper = new JsonSlurper().parseText(loginRequest)

       def access_token = slurper.access_token

				}
	
			}

			stage('Get IDs'){
				steps{
					url = "curl -s -L https://eu1.anypoint.mulesoft.com/accounts/api/me -H \"Authorization: Bearer ${access_token}\"";

					def request = url.execute().text

					slurper = new JsonSlurper().parseText(request)

					orgId = slurper.user.contributorOfOrganizations[0].id

					print "ORG ID ${orgId}"

					url = "curl -s -L https://eu1.anypoint.mulesoft.com/accounts/api/organizations/${orgId}/environments -H \"Authorization: Bearer ${access_token}\"";

					request = url.execute().text

					slurper = new JsonSlurper().parseText(request)

					for(i=0;i < slurper.data.size();i++){
					    
					    if(slurper.data[i].name.equals("DEV")){
					        
					        envId = slurper.data[i].id
					        
					        break;
					        
					    }
					    
					}
					println "ENV ID ${envId}"

				}
			}
			stage('Number of APIs'){

				//{\"query\":\"{assets(query: {type: \\\"rest-api\\\", organizationIds: [\\\"82db8a6b-eef4-4bb8-9b4a-c81e9dd15286\\\"], offset: 0, limit: 1000}) {groupId assetId version}}\"}


				url = "curl -s -L https://eu1.anypoint.mulesoft.com/apimanager/api/v1/organizations/${orgId}/environments/${envId}/apis -H \"Authorization: Bearer ${access_token}\"";
				//url ="curl -H 'Content-Type: application/json' -H \"Authorization: Bearer ${access_token}\" https://eu1.anypoint.mulesoft.com/graph/api/v1/graphql -X POST --data-raw '{\"query\":\"{assets(query: {type: \\\"rest-api\\\", organizationIds: [\\\"82db8a6b-eef4-4bb8-9b4a-c81e9dd15286\\\"], offset: 0, limit: 1000}) {groupId assetId version}}\"}'"
				print url
				request = url.execute().text

				slurper = new JsonSlurper().parseText(request)

				print slurper.total
				println "TOTAL APIS ${slurper.total}"
			}			
		}
	}

