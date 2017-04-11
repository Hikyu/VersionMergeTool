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


