# Mylyn Gitlab Connector                                                                                            
This connector was created after the connector in the Eclipse-Marketplace stopped working (Gitlab removed support for API level bellow v4.                   

### Update-Site
The update-site for the current release can be found here: https://stautob.github.io/mylyn-gitlab/1.0/

### Creating a connector
![CreateConnector](/images/SelectConnector.png)

### Configuring the repository
At the moment support for API login using username/password without O-Auth seems to be broken/removed [see this issue](https://gitlab.com/gitlab-org/gitlab-ce/issues/27793)
The temporary workaround is to log in using a private access token generated in the personal settings in gitlab.

![ConfigureRepository](/images/ConfigureRepository.png)

## Problems                                                                                                         
* The gitlab-api seems not to support username/password login at the moment.                                        
                                                                                                                    
## Todo                                                                                                             
* Update issue-messages to propperly reference changes for labels and other references.     
* Support O-Auth login 
                                                                                                                    
                                                                                                          
