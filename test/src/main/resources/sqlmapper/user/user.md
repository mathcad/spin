getUserDetailByCondition
====
select
    bud.id as id,
    bud.nick_name as nickName,
    u.mobile as mobile,
    u.user_type as userType,
    (select r.name from sys_region r where r.id = bud.province_id) as province,
    (select s.name from sys_region s where s.id = bud.city_id) as city,
    u.create_time as createTime,
    u.active as active
from
	bs_user_detail bud inner join sys_user u on bud.user_id = u.id and u.user_type in (2, 4)
where
    1 = 1
<#if nickName??>
    and bud.nick_name like '%${nickName}%'
</#if>
<#if mobile??>
    and u.mobile like '%${mobile}%'
</#if>
<#if province??>
    and bud.province_id = ${province}
</#if>
<#if city??>
    and bud.city_id = ${city}
</#if>
<#if active??>
    and bud.active = ${active}
</#if>
    order by bud.create_time desc
    
updateAddr
==== 
update bs_user_detail ud 
set 
    <#if cId??>
    ud.city_id = ${cId},
    </#if>
    <#if dId??>
    ud.district_id = ${dId},
    </#if>
    ud.province_id = ${pId}
where
    ud.id = ${udId}