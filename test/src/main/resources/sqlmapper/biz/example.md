test
===
```js
var table = '';
var v = value;
switch (v) {
    case 1:
        table = 'demo1';
        break;
    case 2:
        table="demo2";
        break;
}
```
select ${enum('org.spin.data.sql.BeetlTest.Type', 'a.type', 'type')} from ${table};
