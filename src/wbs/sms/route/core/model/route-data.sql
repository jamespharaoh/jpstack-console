
-- route

SELECT object_type_insert (
	'route',
	'route',
	'root',
	1);

SELECT priv_type_insert (
	'route',
	'manage',
	'Manage route',
	'',
	true);

SELECT priv_type_insert (
	'route',
	'test',
	'Insert test messages',
	'',
	true);

SELECT priv_type_insert (
	'route',
	'stats',
	'View stats',
	'',
	true);

SELECT priv_type_insert (
	'route',
	'send',
	'???',
	'',
	true);

SELECT priv_type_insert (
	'route',
	'messages',
	'View messages',
	'',
	true);

-- slice

SELECT priv_type_insert (
	'slice',
	'route_create',
	'Create routes',
	'Create new routes in this slice',
	true);