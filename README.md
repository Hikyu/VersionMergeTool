# VersionMergeTool
代码合并工具

## 背景

现有工具DBStudio是从开源代码DBEaver上移植而来的。开发工作完成之后，DBEaver官方代码已经迭代了数个版本，需要DBStudio跟新。

每个版本的迭代少则数百个文件，多则上千个文件。完全的手动跟新费时费力(眼都要瞎了)，其中的部分文件是可以实现自动更新的，思路如下。

## 思路

版本合并工具 思路：以Dbstudio 更新到 dbeaver 3.7.8为例。设当前Dbstudio已经更新到dbeaver3.7.5

 1. 找出Dbstudio与dbeaver3.7.5中内容相同文件list1，这部分文件是我们没有更改过的 

 2. 找出dbeaver3.7.8与dbeaver3.7.5不内容同的文件list2，这部分是3.7.8相对于3.7.5升级的部分，也是我们要对应升级的部分

 3. 找出list1与list2的交集autoMergeList，这部分是可以直接从3.7.8复制到Dbstudio的，也是可以实现工具自动化更新到部分

 4. 找出list2中存在而list1中没有的文件manualMergeList，这部分文件是我们需要手动修改的文件

 5. 对autoMergeList中相应的文件从dbeaver3.7.8复制到Dbstudio

 5. 打印manualMergeList，这是需要进行手动合并的文件

## 用法

参考App.java

- 1

  // 初始化合并工具

  VersionMergeTool tool = new VersionMergeTool();

- 2

  // 添加合并的目标文件夹

  // oldVer: 旧版本文件夹(dbeaver3.7.5)

  // newVer: 新版本文件夹(dbeaver3.7.8)

  // mergeTo: 目标文件夹(dbstudio)

  // false: 只打印分析结果，true则自动合并

  // 可多次执行addToMergeVersion, 合并工作将顺序执行

  tool.addToMergeVersion(oldVer, newVer, mergeTo, false);

  tool.addToMergeVersion(oldVer, newVer, mergeTo, false);

  ....

- 3

  // 执行

  tool.doMerge();

- 4

  打印结果
```
****************************************************************
oldVersion : Y:\dbaeaver\dbeaver-3.7.5\features\
newVersion : Y:\dbaeaver\dbeaver-3.7.8\features\
mergeTo : Y:\code\dbstudio\features\
****************************************************************
************************listToCopy******************************
File Size: 25
org.jkiss.dbeaver.ext.generic.feature\pom.xml
org.jkiss.dbeaver.runtime.feature\pom.xml
org.jkiss.dbeaver.runtime.feature\feature.xml
org.jkiss.dbeaver.ext.mysql.feature\feature.properties
org.jkiss.dbeaver.ext.db2.feature\feature.properties
org.jkiss.dbeaver.standalone.feature\feature.properties
org.jkiss.dbeaver.ext.generic.feature\feature.properties
org.jkiss.dbeaver.standalone.feature\pom.xml
org.jkiss.dbeaver.ext.mysql.feature\pom.xml
org.jkiss.dbeaver.ext.db2.feature\pom.xml
org.jkiss.dbeaver.ext.oracle.feature\feature.properties
org.jkiss.dbeaver.ext.wmi.feature\feature.properties
org.jkiss.dbeaver.ext.generic.feature\feature.xml
org.jkiss.dbeaver.rcp.feature\pom.xml
org.jkiss.dbeaver.ext.db2.feature\feature.xml
org.jkiss.dbeaver.core.feature\pom.xml
org.jkiss.dbeaver.rcp.feature\feature.xml
org.jkiss.dbeaver.ext.wmi.feature\pom.xml
org.jkiss.dbeaver.ext.oracle.feature\feature.xml
org.jkiss.dbeaver.runtime.feature\feature.properties
org.jkiss.dbeaver.ext.mysql.feature\feature.xml
org.jkiss.dbeaver.ext.oracle.feature\pom.xml
org.jkiss.dbeaver.core.feature\feature.xml
org.jkiss.dbeaver.ext.wmi.feature\feature.xml
org.jkiss.dbeaver.core.feature\feature.properties
auto merge complete!
************************listToManualMerge******************************
File Size: 11
org.jkiss.dbeaver.ce.feature\feature.properties
org.jkiss.dbeaver.ext.exasol.feature\build.properties
org.jkiss.dbeaver.ext.exasol.feature\.project
org.jkiss.dbeaver.ext.exasol.feature\feature.xml
org.jkiss.dbeaver.standalone.feature\feature.xml
org.jkiss.dbeaver.ext.exasol.feature\pom.xml
org.jkiss.dbeaver.ce.feature\build.properties
org.jkiss.dbeaver.ce.feature\pom.xml
org.jkiss.dbeaver.ce.feature\feature.xml
org.jkiss.dbeaver.ce.feature\root\Info.plist
org.jkiss.dbeaver.ext.exasol.feature\feature.properties
```


