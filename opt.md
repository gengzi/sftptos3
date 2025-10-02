### 功能点
* 支持用户管理，管理用户，添加，删除，修改  --待实现
* 支持不同用户访问不同文件系统（本地或者s3） 甚至虚拟的文件系统   --代实现
* 支持用户设置属于自己的根目录，只能访问其下面的内容  
* 支持sftp服务监控，检测当前链接数，检测当前用户使用情况   -- 代实现
* 支持用户自定义访问s3文件系统代码实现，兼容各种s3存储



* 将服务容器化，k8s编排，支持动态扩缩容
* 某个sftptos3容器挂掉，nginx自动去掉该配置，调用其他存活的服务
将某个ip自动映射到某台服务中
* 增加ip白名单机制，只有白名单中的ip才能访问系统

* 优化jvm，发现占用的堆内存比较大


* 定时任务，定时删除7天的前的临时文件（如果开启审计，加入到审计中）










### 问题点
* 创建空目录，删除后。在父目录调用listobject 依然存在
```angular2html
问题原因：
实际删除空目录时，删除的是之前创建的空字节对象（xx/yy/ 类似这样的），其实已经删除了
但是使用的是minio测试的，发现删除空字节对象删除成功，但是listobject 仍然存在，导致还能获取到的该目录
使用minio点击删除，发现也不会删除掉
可能使用其他的s3文件系统不会存在该问题
```



### 优化
* 问题1：java.util.concurrent.ExecutionException: software.amazon.awssdk.core.exception.SdkClientException: Unable to execute HTTP request: Java heap space (SDK Attempt Count: 1)
```
优化点：
1，堆内存溢出，使用直接内存（堆外内存需要手动移除） ,
2，限制并发读取数
3，增加堆内存
4，修改缓存的内容大小，分片数。更少更小的内存占用（但是会调用s3api更多）
```
* 问题2：sftp下载文件默认大小为32kb，这样会导致从s3下载文件时会每次都读取32kb数据，会频繁调用s3
```angular2html
优化点：
预先读取缓存到内存中，每次读取缓存中的数据，当缓存中的数据不够时，再从s3下载数据
```

* 问题3：当s3某个相同key前缀存放太多文件时（可以简单理解为某个目录下存放了大量的文件），在sftp操作时会频繁获取某个对象的属性信息（大小，
是否为文件，等信息）会频繁调用s3api
```angular2html
优化点：
1，使用缓存，缓存对象信息，缓存对象内容，缓存对象列表信息，在操作删除，重命名，写入等操作时再移除缓存
2，在获取文件或者目录属性时，判断是目录时，会调用s3，把目录作为前缀扫描目录下有什么文件或者目录前缀，
在这个过程中已经获取到该目录下的元素的信息。所有也会把这部分数据进行缓存
```
* 问题4：从s3获取的目录前缀的对象时，会在“路径”后添加一个 /，这样会导致一部分sftp客户端获取目录失败（winscp）
```angular2html
优化点：
在获取s3对象列表时，将 / 移除
```
* 问题5：因为目录时空字节对象创建的，导致调用s3 listobject时响应结果中在object的节点下会发现该空字节对象，但是实际上在sftp实际为一个目录
```angular2html
优化点：
针对空字节对象单独处理，将其缓存为目录
```
* 问题6：上传文件时，临时目录创建的文件并没有删除掉。可能是上传一半不上传，或者报异常了。
```angular2html
优化点：调整为上传s3报异常，依然删除临时本地文件
```
* 问题7: 发现force() 方法，在sftp执行时会调用，此方法也会上传文件到s3.如果发生异常，后续channel的colse就不执行了
```angular2html
优化点：
1，在sftp执行force()方法时，上传文件到s3，如果发生异常，将异常捕获不抛出
```












loan-sftp 实现一个高可用的sftp 服务
* 支持sftp客户端链接服务
* 高可用（多个服务实例同时支持）
* 保证文件一致性


使用开源项目  Spring Integration  FTP/FTPS 实现



* 接受客户端请求处理类 ：上传，下载，认证


上传文件：
先写入临时文件中，当判断最后一次写入时，就把临时文件移动到正式目录下

下载文件：
判断当前文件不存在，就从数据库中获取这个文件的真实服务器和下载地址。
读取时，从对应的服务器获取文件部分内容


- 命令行登录sftp服务
  sftp -P 2222 admin@172.30.96.1
  type:1  type:200  type:16

ls
type:11(SSH_FXP_OPENDIR)  type:12(SSH_FXP_READDIR)  type:4(SSH_FXP_CLOSE)


get /s3/file/1.txt


put ./2.txt /s3/file/
type=17

put ./2.txt
type=3 typeName= open

-- 密钥登录
sftp  -i E:\ssh\id_rsa -P 2222 admin@127.0.0.1




acl,[BUILTIN\Administrators:READ_DATA/WRITE_DATA/APPEND_DATA/READ_NAMED_ATTRS/WRITE_NAMED_ATTRS/EXECUTE/DELETE_CHILD/READ_ATTRIBUTES/WRITE_ATTRIBUTES/DELETE/READ_ACL/WRITE_ACL/WRITE_OWNER/SYNCHRONIZE:ALLOW, NT AUTHORITY\SYSTEM:READ_DATA/WRITE_DATA/APPEND_DATA/READ_NAMED_ATTRS/WRITE_NAMED_ATTRS/EXECUTE/DELETE_CHILD/READ_ATTRIBUTES/WRITE_ATTRIBUTES/DELETE/READ_ACL/WRITE_ACL/WRITE_OWNER/SYNCHRONIZE:ALLOW, NT AUTHORITY\Authenticated Users:READ_DATA/WRITE_DATA/APPEND_DATA/READ_NAMED_ATTRS/WRITE_NAMED_ATTRS/EXECUTE/READ_ATTRIBUTES/WRITE_ATTRIBUTES/DELETE/READ_ACL/SYNCHRONIZE:ALLOW, BUILTIN\Users:READ_DATA/READ_NAMED_ATTRS/EXECUTE/READ_ATTRIBUTES/READ_ACL/SYNCHRONIZE:ALLOW]
archive,true
attributes,524320
creationTime,2025-08-04T02:50:22.0983764Z
fileKey,null
hidden,false
isDirectory,false
isOther,false
isRegularFile,true
isSymbolicLink,false
lastAccessTime,2025-08-05T10:17:18.4035846Z
lastModifiedTime,2025-08-04T02:58:18.3907234Z
owner,BUILTIN\Administrators (Alias)
permissions,[OWNER_READ, OWNER_WRITE, GROUP_READ, GROUP_WRITE, OTHERS_READ, OTHERS_WRITE]
readonly,false
size,285
system,false





打开文件，返回一个文件句柄（已经存储了一个文件通道）
读取文件，根据文件句柄，拿到存储的文件通道，根据文件通道实例获取文件内容（分片的内容）
返回分片内容
继续读取文件，直到文件读取完毕
关闭文件，根据文件句柄，拿到存储的文件通道，关闭文件通道




### minio
win 启动命令
* E:\ruanjian\minio.exe server  F:\sso
  访问地址：http://127.0.0.1:9000




### apche  sshd sftp
---  不允许删除目录
```
org.apache.sshd.sftp.server.AbstractSftpSubsystemHelper.doRemoveFile
else if (Files.isDirectory(p, options)) {
throw signalRemovalPreConditionFailure(id, path, p,
new SftpException(SftpConstants.SSH_FX_FILE_IS_A_DIRECTORY, p.toString() + " is a folder"), false);
}
```


### 性能监控 arthas
java -jar E:\ruanjian\arthas\arthas-boot.jar


### 性能优化
需要增加缓存，避免经常访问s3存储，大量请求会拖慢
针对频繁请求的流程，和即时性不强的数据

优化删除逻辑，只允许删除文件，不允许删除目录

trace:

### 密钥生成
```angular2html
# 生成 RSA 密钥对（-t 指定算法，-b 指定密钥长度，-C 添加注释）
ssh-keygen -t rsa -b 2048 -C "your_email@example.com"
```


### 使用堆外空间存储（直接存储）
```
需要在运行前添加jvm 参数：
--add-opens java.base/java.nio=ALL-UNNAMED --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/jdk.internal.ref=ALL-UNNAMED
并修改配置文件，开启堆外存储
```




## 服务器部署方案
* docker服务部署
```angular2html



docker-compose up -d --build

```
* 采用minio开源s3部署
```angular2html
-- 后台运行，监控台端口为9001，默认s3运行端口为9000
nohup ./minio server /path/to/data   --console-address ":9001" > minio.log 2>&1 &
```

