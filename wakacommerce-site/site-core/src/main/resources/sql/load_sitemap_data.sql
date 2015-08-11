INSERT INTO BLC_MODULE_CONFIGURATION (MODULE_CONFIG_ID,CONFIG_TYPE,IS_DEFAULT,MODULE_NAME,MODULE_PRIORITY,ACTIVE_START_DATE) VALUES (-1,'SITE_MAP',TRUE,'SITE_MAP',100,CURRENT_TIMESTAMP);
INSERT INTO BLC_SITE_MAP_CFG (MODULE_CONFIG_ID) VALUES (-1);
INSERT INTO BLC_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,MODULE_CONFIG_ID,DISABLED,CHANGE_FREQ,GENERATOR_TYPE,PRIORITY) VALUES (-1,-1,FALSE,'HOURLY','自定义','0.5');
INSERT INTO BLC_CUST_SITE_MAP_GEN_CFG (GEN_CONFIG_ID) VALUES (-1);
INSERT INTO BLC_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,MODULE_CONFIG_ID,DISABLED,CHANGE_FREQ,GENERATOR_TYPE,PRIORITY) VALUES (-2,-1,FALSE,'HOURLY','商品','0.5');
INSERT INTO BLC_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,MODULE_CONFIG_ID,DISABLED,CHANGE_FREQ,GENERATOR_TYPE,PRIORITY) VALUES (-3,-1,FALSE,'HOURLY','页面','0.5');
INSERT INTO BLC_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,MODULE_CONFIG_ID,DISABLED,CHANGE_FREQ,GENERATOR_TYPE,PRIORITY) VALUES (-4,-1,FALSE,'HOURLY','商品分类','0.5');
INSERT INTO BLC_CAT_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,ROOT_CATEGORY_ID,STARTING_DEPTH,ENDING_DEPTH) VALUES (-4,2001,0,0);
INSERT INTO BLC_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,MODULE_CONFIG_ID,DISABLED,CHANGE_FREQ,GENERATOR_TYPE,PRIORITY) VALUES (-5,-1,FALSE,'HOURLY','商品分类','0.5');
INSERT INTO BLC_CAT_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,ROOT_CATEGORY_ID,STARTING_DEPTH,ENDING_DEPTH) VALUES (-5,2002,0,0);
INSERT INTO BLC_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,MODULE_CONFIG_ID,DISABLED,CHANGE_FREQ,GENERATOR_TYPE,PRIORITY) VALUES (-6,-1,FALSE,'HOURLY','商品分类','0.5');
INSERT INTO BLC_CAT_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,ROOT_CATEGORY_ID,STARTING_DEPTH,ENDING_DEPTH) VALUES (-6,2003,0,0);
INSERT INTO BLC_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,MODULE_CONFIG_ID,DISABLED,CHANGE_FREQ,GENERATOR_TYPE,PRIORITY) VALUES (-7,-1,FALSE,'HOURLY','商品分类','0.5');
INSERT INTO BLC_CAT_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,ROOT_CATEGORY_ID,STARTING_DEPTH,ENDING_DEPTH) VALUES (-7,2004,0,0);
INSERT INTO BLC_SITE_MAP_GEN_CFG (GEN_CONFIG_ID,MODULE_CONFIG_ID,DISABLED,CHANGE_FREQ,GENERATOR_TYPE,PRIORITY) VALUES (-8,-1,TRUE,'HOURLY','SKU','0.5');

INSERT INTO BLC_SITE_MAP_URL_ENTRY (URL_ENTRY_ID,GEN_CONFIG_ID,LAST_MODIFIED,LOCATION,CHANGE_FREQ,PRIORITY) VALUES (-1,-1,CURRENT_TIMESTAMP,'http://localhost/1','HOURLY','0.5');
INSERT INTO BLC_SITE_MAP_URL_ENTRY (URL_ENTRY_ID,GEN_CONFIG_ID,LAST_MODIFIED,LOCATION,CHANGE_FREQ,PRIORITY) VALUES (-2,-1,CURRENT_TIMESTAMP,'http://localhost/2','HOURLY','0.5');
INSERT INTO BLC_SITE_MAP_URL_ENTRY (URL_ENTRY_ID,GEN_CONFIG_ID,LAST_MODIFIED,LOCATION,CHANGE_FREQ,PRIORITY) VALUES (-3,-1,CURRENT_TIMESTAMP,'http://localhost/3','HOURLY','0.5');
INSERT INTO BLC_SITE_MAP_URL_ENTRY (URL_ENTRY_ID,GEN_CONFIG_ID,LAST_MODIFIED,LOCATION,CHANGE_FREQ,PRIORITY) VALUES (-4,-1,CURRENT_TIMESTAMP,'http://localhost/4','HOURLY','0.5');
INSERT INTO BLC_SITE_MAP_URL_ENTRY (URL_ENTRY_ID,GEN_CONFIG_ID,LAST_MODIFIED,LOCATION,CHANGE_FREQ,PRIORITY) VALUES (-5,-1,CURRENT_TIMESTAMP,'http://localhost/5','HOURLY','0.5');
INSERT INTO BLC_SITE_MAP_URL_ENTRY (URL_ENTRY_ID,GEN_CONFIG_ID,LAST_MODIFIED,LOCATION,CHANGE_FREQ,PRIORITY) VALUES (-6,-1,CURRENT_TIMESTAMP,'http://localhost/6','HOURLY','0.5');
INSERT INTO BLC_SITE_MAP_URL_ENTRY (URL_ENTRY_ID,GEN_CONFIG_ID,LAST_MODIFIED,LOCATION,CHANGE_FREQ,PRIORITY) VALUES (-7,-1,CURRENT_TIMESTAMP,'http://localhost/7','HOURLY','0.5');
INSERT INTO BLC_SITE_MAP_URL_ENTRY (URL_ENTRY_ID,GEN_CONFIG_ID,LAST_MODIFIED,LOCATION,CHANGE_FREQ,PRIORITY) VALUES (-8,-1,CURRENT_TIMESTAMP,'http://localhost/8','HOURLY','0.5');
