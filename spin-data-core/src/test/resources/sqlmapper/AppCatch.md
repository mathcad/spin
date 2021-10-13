selectSystemRecord
===
select id,
device_code deviceCode,
cash_msg cashMsg,
com_name comName,
version_code versionCode,
create_time createTime
from app_catch t where 1=1
${valid(deviceCode, "and t.device_code = '" + deviceCode + "'")}
${valid(comName, "and t.com_name like '%" + comName + "%'")}
${valid(versionCode, "and t.version_code = '" + versionCode + "'")}

test
===
select * from auth
where 1=1
and code = :code
${valid(a, "and t.device_code = aa")}

${sqlIn("and xxx","a", a)}

@if (a.~size > 0) {
and id in (
    @for(var i=0;i<a.~size;i++) {
'${a[i]}'${(i == a.~size-1 ? '' : ',')}
    @}
)
@}
