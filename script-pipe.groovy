import groovy.json.JsonSlurper


pipeline{
	agent any 
		stages{
			stage('Login'){

				steps{						
					script{
						println ${params.user}
						def loginRequest =   "curl -H 'Content-Type: application/x-www-form-urlencoded' -X POST -d username=${params.user} -d password=${params.password} https://eu1.anypoint.mulesoft.com/accounts/login".execute().text
						def slurper = new JsonSlurper().parseText(loginRequest)
						def access_token = slurper.access_token						
						println 'LOGIN IN ANYPOINT PLATFORM'
		
						println ${access_token}

       					

					}

								
				}
	
			}

			stage('Get IDs'){
				steps{
					script{

					url = "curl -s -L https://eu1.anypoint.mulesoft.com/accounts/api/me -H \"Authorization: Bearer ${access_token}\"";

					def request = url.execute().text

					slurper = new JsonSlurper().parseText(request)

					def orgId = slurper.user.contributorOfOrganizations[0].id

					print "ORG ID ${orgId}"

					url = "curl -s -L https://eu1.anypoint.mulesoft.com/accounts/api/organizations/${orgId}/environments -H \"Authorization: Bearer ${access_token}\"";

					request = url.execute().text

					slurper = new JsonSlurper().parseText(request)

					for(i=0;i < slurper.data.size();i++){
					    
					    if(slurper.data[i].name.equals("DEV")){
					        
					        def envId = slurper.data[i].id
					        
					        break;
					        
					    }
					    
					}
					println "ENV ID ${envId}"


					}
					
				}
			}
			stage('Number of APIs'){
					steps{
						script{
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
				
				}
			}

