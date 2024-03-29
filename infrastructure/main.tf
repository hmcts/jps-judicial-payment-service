provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

locals {
  app_full_name = "${var.product}-${var.component}"

  // Shared Resource Group
  sharedResourceGroup = "${var.raw_product}-shared-${var.env}"

  // Vault name
  vaultName = "${var.raw_product}-${var.env}"
}

data "azurerm_key_vault" "jps_shared_key_vault" {
  name                = local.vaultName
  resource_group_name = local.sharedResourceGroup
}

module "postgresql_v15" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "cft"
  common_tags          = var.common_tags
  component            = var.component
  env                  = var.env
  pgsql_databases = [
    {
      name = var.database_name
    }
  ]
  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "plpgsql,pg_stat_statements,pg_buffercache,hypopg"
    }
  ]
  pgsql_version    = "15"
  product          = var.product
  name             = "${local.app_full_name}-postgres-db-v15"
  pgsql_sku        = var.pgsql_sku
  pgsql_storage_mb = var.pgsql_storage_mb
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////
resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = "${var.component}-POSTGRES-USER"
  value        = module.postgresql_v15.username
  key_vault_id = data.azurerm_key_vault.jps_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.postgresql_v15.password
  key_vault_id = data.azurerm_key_vault.jps_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-HOST" {
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.postgresql_v15.fqdn
  key_vault_id = data.azurerm_key_vault.jps_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value     =  var.postgresql_flexible_server_port
  key_vault_id = data.azurerm_key_vault.jps_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = var.database_name
  key_vault_id = data.azurerm_key_vault.jps_shared_key_vault.id
}
