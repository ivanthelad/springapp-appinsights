## Install Azure Keyvault CSI Driver

```
helm repo add csi-secrets-store-provider-azure https://raw.githubusercontent.com/Azure/secrets-store-csi-driver-provider-azure/master/charts
helm install csi-secrets-store-provider-azure/csi-secrets-store-provider-azure --generate-name
```
 
 
            
            
## Create a Keyvault
```
KEYVAULT_NAME=demokeyvaulthow2
RESOURCE_GROUP=demokeyvaulthow2
LOCATION=westeurope
az keyvault create --name $KEYVAULT_NAME --resource-group $RESOURCE_GROUP --location $LOCATION 
```

### Create the Azure Keyvault Secret
add the secrets to keyvault 
```
az keyvault secret set --vault-name $KEYVAULT_NAME --name POSTGRES-USER --value postgres
az keyvault secret set --vault-name $KEYVAULT_NAME --name POSTGRES-PASS --value postgres
az keyvault secret set --vault-name $KEYVAULT_NAME --name APPLICATIONINSIGHTS-CONNECTION-STRING --value postgres
```

## Grant Cluster Access KeyVault using managed identities 
if your  cluster is using manged identity(VMSS) then we need to figure out the principal ID and set it in the provider class `"userAssignedIdentityID=".`The driver will use this identity when `useVMManagedIdentity=true`. 
Get the  clientId of the kublet identity by executing the following. see [details](https://github.com/Azure/secrets-store-csi-driver-provider-azure/blob/master/docs/user-assigned-msi-mode.md)
```
export AZURE_CLIENT_ID=$(az aks show -g coredns-68e4f -n coredns-68e4f --query identityProfile.kubeletidentity.clientId -o tsv)
az aks show -g coredns-68e4f -n coredns-68e4f --query identityProfile.kubeletidentity.clientId -o tsv
```

This kubelet clientId will be required for the next steps when creating the `SecretProviderClass`



### Grant kublet clientid identity  access to keyvault secrets 
where `$KEYVAULT_NAME` is the keyvault you created and $AZURE_CLIENT_ID is the system assigned identity [see]( https://docs.microsoft.com/en-us/azure/key-vault/general/key-vault-integrate-kubernetes#create-your-own-secretproviderclass-object)
```
subID=<subscription id>
RESOURCE_GROUP=<resourcegroup_kv>
export AZURE_CLIENT_ID=$(az aks show -g <aks_resourcegroup> -n <aks_clustername> --query identityProfile.kubeletidentity.clientId -o tsv)
az role assignment create --role Reader --assignee $AZURE_CLIENT_ID  --scope /subscriptions/$subID/resourcegroups/$RESOURCE_GROUP/providers/Microsoft.KeyVault/vaults/$KEYVAULT_NAME
az keyvault set-policy -n $KEYVAULT_NAME --secret-permissions get --object-id $AZURE_CLIENT_ID
az keyvault set-policy -n $KEYVAULT_NAME --key-permissions get --object-id $AZURE_CLIENT_ID 
```

### Create the  SecretProviderClass
update the following fields the the file secretProviderClass
  *  keyvaultName: "" # [REQUIRED] the keyvault name 
  *  resourceGroup: ""     # [REQUIRED] the resource group name of the key vault
  *  subscriptionId: ""          # [REQUIRED] the subscription ID of the key vault. Can be found by executing 'az account show'
  *  tenantId: ""   # [REQUIRED] the tenant id. Can be found by executing 'az account show'
  * userAssignedIdentityID: ""  # [REQUIRED]  the client id of the kublet managed identity
  
note: for this case we are mounting the secrets but also want to use it as an environment variable. for this case we create an object called "secretObjects". this tells provider to also create kubernetes [secrets](https://github.com/Azure/secrets-store-csi-driver-provider-azure#optional-sync-with-kubernetes-secrets  ) 
note: it is strongly recommended to use pod identity to fetch keyvault secrets

```
apiVersion: secrets-store.csi.x-k8s.io/v1alpha1
kind: SecretProviderClass
metadata:
  name: azure-kvname
spec:
  provider: azure
  secretObjects: # [OPTIONAL] SecretObject defines the desired state of synced K8s secret objects
    - secretName: mysecret                     # name of the Kubernetes Secret object
      type: Opaque                              # type of the Kubernetes Secret object e.g. Opaque, kubernetes.io/tls
      data:
        - key: POSTGRES-USER                           # data field to populate
          objectName: POSTGRES-USER                        # name of the mounted content to sync. this could be the object name or the object alias
        - key: APPLICATIONINSIGHTS-CONNECTION-STRING
          objectName: APPLICATIONINSIGHTS-CONNECTION-STRING
        - key: POSTGRES-PASS
          objectName: POSTGRES-PASS
  parameters:
    usePodIdentity: "false"                   # [REQUIRED] Set to "true" if using managed identities
    useVMManagedIdentity: "true"             # [OPTIONAL] if not provided, will default to "false"
    userAssignedIdentityID: "<clientid>"        # [REQUIRED] If you're using a service principal, use the client id to specify which user-assigned managed identity to use. If you're using a user-assigned identity as the VM's managed identity, specify the identity's client id. If the value is empty, it defaults to use the system-assigned identity on the VM
      #     az ad sp show --id http://contosoServicePrincipal --query appId -o tsv
    #     the preceding command will return the client ID of your service principal
    keyvaultName: "<keyvault>"          # [REQUIRED] the name of the key vault
      #     az keyvault show --name contosoKeyVault5
    #     the preceding command will display the key vault metadata, which includes the subscription ID, resource group name, key vault
    cloudName: "AzurePublicCloud"                                # [OPTIONAL for Azure] if not provided, Azure environment will default to AzurePublicCloud
    resourceGroup: "<keyvault_resourcegroup>"     # [REQUIRED] the resource group name of the key vault
    subscriptionId: "<subscription_id>"          # [REQUIRED] the subscription ID of the key vault
    tenantId: "<tenant_id>"                      # [REQUIRED] the tenant ID of the key vault
    objects:  |
      array:
        - |
          objectName: POSTGRES-USER                 # [REQUIRED] object name
                                              #     az keyvault secret list --vault-name "contosoKeyVault5"
                                              #     the above command will display a list of secret names from your key vault
          objectType: secret                  # [REQUIRED] object types: secret, key, or cert
          objectAlias: POSTGRES-USER
        - |
          objectName: POSTGRES-PASS
          objectAlias: POSTGRES-PASS
          objectType: secret
        - |
          objectName: APPLICATIONINSIGHTS-CONNECTION-STRING
          objectType: secret
          objectAlias: APPLICATIONINSIGHTS-CONNECTION-STRING

```





### Notes: 
AKS with managed identity 
* https://docs.microsoft.com/en-us/azure/aks/use-managed-identity#create-an-aks-cluster-with-managed-identities
* https://github.com/Azure/secrets-store-csi-driver-provider-azure
* https://docs.microsoft.com/en-us/azure/key-vault/general/key-vault-integrate-kubernetes#create-your-own-secretproviderclass-object


