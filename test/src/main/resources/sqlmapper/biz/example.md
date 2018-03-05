test
===
```js
switch (value) {
    case 1:
        table = 'demo_a';
        break;
    case 2:
        table="demo_b";
        break;
    default:
        table="undefined";
}
```
select
  ${enum('com.shipping.domain.enums.OrganizationTypeE', 'a.type', 'type')}
from ${table} t
where 1=1
  ${valid(table, "and t.name like '" + table + "'")}
  ${has(flag) ? "and t.flag = " + flag}
  
test2
===
aaaaaaaaaaa
