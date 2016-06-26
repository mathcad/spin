<?xml version="1.0" encoding="utf-8"?>
<commands>
    <command id="findProductTarget">
        <![CDATA[
        SELECT
        q.id as qid,
        v.id as vid,
        v.plate,
        v.quote_type,
        ${E('org.infrastructure.jpa.core.sqlmap.VhcQuoteTypeE','v.quote_type','type_name')},
        FROM
        bs_motorcade_organ_user mu,
        bs_motorcade_vhc_user vu,
        bs_motorcade_vhc mv,
        bs_motorcade m,

        td_quote q,
        td_order td,
        td_charge chr,
        bs_vhc v,
        bs_dict type,
        bs_dict mode
        WHERE
        mu.id=vu.user
        AND vu.vhc = mv.id
        and mv.vhc=v.id
        and mu.mtcade = m.id

        and v.id=q.vhc
        and q.tdo=td.id
        and td.status=1
        and td.id = chr.tdo
        and mode.id= chr.insurance_type
        and td.type= type.id
        AND td.sub_mode IN (0,1)
        and mu.user = :user
    <#if V(no)>
        and td.no like '%${no}%'
    </#if>
    <#if V(plate)>
        and v.plate like '%${plate}%'
    </#if>
        ]]>
    </command>
</commands>