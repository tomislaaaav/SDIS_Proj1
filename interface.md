<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- saved from url=(0075)https://web.fe.up.pt/~pfs/aulas/sd2016/projs/proj1/proj1_svc_interface.html -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style type="text/css"></style></head>

<body>
<h2>SDIS 2015/2016 - 2nd Semester<br>
	 Project 1 -- Distributed Backup Service Interface<br>
</h2>
<hr>	

<h2>1. Introduction</h2>

<p>These notes specify the interface that you will have to develop for your Distributed Backup Service. Basically, this interface will allow us to invoke the sub protocols provided by that service to backup, restore and delete files, as well as to reclaim space being used by the service.</p>

<p>The main reason for this specification is to make it straightforward to test your implementation of the service, and therefore reduce the time required for that test in the two classes following the submission of your service. Past experience has been that if we leave the interface to you, some groups take forever to setup their services wasting everybody's time.</p>

<p>The idea is that you will have to develop a small testing application with a command line interface (CLI) that triggers the execution of the different subprotocols depending on its input arguments. For that, you will have to define a simple protocol that will allow the testing application to communicate to one peer that will play the role of initiator of the subprotocol being tested.</p>

<p>These notes have three additional sections. In the next section, we define the CLI. In the following section we make some suggestions regarding the implementation of this interface. Finally, in the last section we make a some recommendations to take into account in the implementation of your service, with its testing in mind.</p>

<h2>2. Testing Application Interface</h2>

<p>As mentioned above, the testing application must have a command line interface. I.e. it should be invoked as follows:</p>

<div align="left" style="background-color: #E6E6FA;; white-space: pre; "><code>$ java TestApp &lt;peer_ap&gt; &lt;sub_protocol&gt; &lt;opnd_1&gt; &lt;opnd_2&gt;</code> </div>

where:
<dl>
	<dt><b>&lt;peer_ap&gt;</b></dt>
	<dd>Is the local peer access point. This depends on the implementation. (Check the next section)</dd>
	<dt><b>&lt;sub_protocol&gt;</b></dt>
	<dd>Is the sub protocol being tested, and must be one of: <code>BACKUP</code>, <code>RESTORE</code>, <code>DELETE</code>, <code>RECLAIM</code>. In the case of enhancements, you must append the substring <code>ENH</code> at the end of the respecive subprotocol, e.g. <code>BACKUPENH</code> </dd>
	<dt><b>&lt;opnd_1&gt;</b></dt>
	<dd>Is either the path name of the file to backup/restore/delete, for the respective 3 subprotocols, or the amount of space to reclaim. In the latter case, the peer should execute the <code>RECLAIM</code> protocol, upon deletion of any chunk.</dd>
	<dt><b>&lt;opnd_2&gt;</b></dt>
	<dd>This operand is an integer that specifies the desired replication degree and applies only to the backup protocol (or its enhancement)</dd>
</dl>

E.g., by invoking:
<div align="left" style="background-color: #E6E6FA;; white-space: pre; "><code>
$ java TestApp 1923 BACKUP test1.pdf 3
</code> </div>
your <code>TestApp</code> is supposed to trigger the backup of file <code>test1.pdf</code> with a replication degree of 3. Likewise, by invoking:
<div align="left" style="background-color: #E6E6FA;; white-space: pre; "><code>
$ java TestApp 1923 RESTORE test1.pdf
</code> </div>
your <code>TestApp</code> is supposed to trigger the restauration of file <code>test1.pdf</code> that was previously replicated.

<h2>3. Testing Application Implementation</h2>

<p>The testing application and your peers are different applications, and they should communicate by exchanging messages. Essentially, for testing purposes, the testing application is a client and the peers are servers. Therefore, you should define a client/server protocol between the testing application and a peer, and the testing application and the peer must implement the respective role of that protocol.</p>

<p>You can use whatever "transport protocol" you deem appropriate, e.g. UDP, TCP or even RMI. However, your choice of transport protocol will affect the syntax of the access point used in the invocation of the testing app.</p>
<p>If you use either UDP or TCP, the format of the access point must be <code>&lt;IP address&gt;:&lt;port number&gt;</code>, where <code>&lt;IP address&gt;</code> and <code>&lt;port number&gt;</code> are respectively the IP address and the port number being used by the (initiator) peer to provide the testing service. If the access point includes only a port number (with or without <code>':'</code>), then you should assume that the initiator server runs on the local host, i.e. the same host as the testing application.</p>
<p>If you choose to implement this interface using RMI you should use the name of the remote object providing the "testing" service.</p>

<p>To avoid modifying the definition of the command line of the peer, i.e. adding yet another argument, you should use the first argument not only as the identifier of the peer but also as the peer access point for the testing service. E.g. if you choose to use either UDP or TCP as transport protocol, then the port number on which the peer will provide the testing service will also be the peer's identifier. If you choose RMI, then you should specify the name of the remote object on which the peer will provide the testing service. Note that the name of a remote object in RMI is an arbitrary string, therefore you can use a string of digits, if your implementation of the peer requires the implementation to be numbers.</p>

<h2>4. Testing Considerations</h2>

<p>In this section we make a few considerations regarding the testing/grading of the four subprotocols.</p>

<h3>4.1 Backup Subprotocol</h3>

<p>According to the specification above, the testing application receives the name of a file to be backed up with some replication degree. The expected output of this command is the replication of all the chuncks that makeup the file to be replicated.</p>

<p>Neither this document nor the document with the protocols specification specifies the division of labor between the replication application and the initiator peer. That is, we do not specify which of these two applications should do the splitting of the file in chunks. It is up to you to decide whether the splitting of a file is done by the testing application or by the peer itself.</p>

<p>Remember that a peer must never store chunks of the files it backups.</p>

<h3>4.2 Restore Subprotocol</h3>

<p>Again, the specification above does not specify whether the restoration/recreation of a file should be performed by the testing application or the peer server itself. It is up to you to decide.</p>

<p>You should also avoid triggering the restoration of a file automatically upon deletion of a file. Not only this requires some additional work, but also may not be always appropriate.</p>

<p>Conversely, the restoration of a file should be performed whenever requested by the testing application to the initiator peer, independently of the existence of the original file. To avoid naming conflicts, you may wish to use either prefix and/or suffix strings in the name of the restored file.</p>

<h3>4.3 File Deletion Subprotocol</h3>

<p>Although the file deletion subprotocol is supposed to support the deletion of a file, you should not trigger the deletion subprotocol automatically when a file is deleted. Instead, the file deletion subprotcol should be triggered explicitly by the testing application.</p>

<h3>4.4 Space Reclaim Subprotocol</h3>

<p>Upon invocation of space reclaim suprotocol via the testing application, your implementation should delete enough chunks so as to ensure that it does not use more disk spaced than allowed by the owner. Again, whether the deletion of the chunks should be done by the testing application or by the peer, is up to you to decide. Nevertheless, we expect the peer to initiate the space reclaim subprotocol.</p>

<p>So as not to constrain testing, you should not impose artificial limits on the amount of space to reclaim. E.g. it should be possible to reclaim all the space that was previously reserved for the backing up of chuncks.</p>


</body></html>