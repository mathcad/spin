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

select ${enum('org.spin.data.sql.BeetlTest$Type', 'a.type', 'type')} from ${table} t where 1=1 ${valid(table, "and t.name like '" + table + "'")}
${has(flag) ? "and t.flag = " + flag} @var a = [1,2,3]; ${valid(a, 'and t.id in ')} @for(var i=0;i<a.~size;i++){ ${a[i]} @} ${valid(a, ')')}

test2
===
aaaaaaaaaaa
