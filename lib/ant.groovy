
/**
 * Log into a remote host and run a given command.
 * 
 * e.g. 
 * sshexec(command: "ps", host: "filebot.sf.net", username: "rednoah", password: "correcthorsebatterystaple")
 */
def sshexec(param) {
	param << [trust: true] // auto-trust remote hosts
	
	_guarded {
		ant().sshexec(param)
	}
}


/**
 * Send email via smtp.
 * 
 * e.g. 
 * sendmail(mailhost:'smtp.gmail.com', mailport:'587', ssl:'no', enableStartTLS:'yes', user:'rednoah@gmail.com', password:'correcthorsebatterystaple', from:'rednoah@gmail.com', to:'someone@gmail.com', subject:'Hello Ant World', message:'Dear Ant, ...')
 */
def sendmail(param) {
	def sender    = param.remove('from')
	def recipient = param.remove('to')
	
	_guarded {
		ant().mail(param) {
			from(address:sender)
			to(address:recipient)
		}
	}
}


/**
 * Send email using gmail default settings.
 *
 * e.g.
 * sendGmail(subject:'Hello Ant World', message:'Dear Ant, ...', to:'someone@gmail.com', user:'rednoah', password:'correcthorsebatterystaple')
 */
def sendGmail(param) {
	param << [mailhost:'smtp.gmail.com', mailport:'587', ssl:'no', enableStartTLS:'yes']
	param << [user:param.username ? param.remove('username') + '@gmail.com' : param.user]
	param << [from: param.from ?: param.user]
	
	sendmail(param)
}


/**
 * Upload files via SCP/SFTP
 *
 * e.g.
 * scp(host: 'filebot.net', username: 'rednoah', password: 'correcthorsebatterystaple', file: '/local/file', remoteFile: '/remote/file')
 * scp(host: 'filebot.net', username: 'rednoah', password: 'correcthorsebatterystaple', dir: '/local/dir', remoteDir: '/remote/dir')
 */
def scp(param) {
	def param_scp = [:]
	def param_fileset = [:]
	
	def remotePath = { f ->
		if (f == null)
			throw new IllegalArgumentException('Remote path not defined: ' + param)
		
		return param.username + (param.password ? ':' + param.password : '') + '@' + param.host + ':' + f.toString().replace('\\', '/')
	}
	
	// user[:password]@host:/directory/path
	if (param.file == null){
		param_scp.remoteTodir = remotePath(param.remoteDir)
		param_fileset.dir = param.dir as String
		param_fileset.includes = (param.includes == null) ? '**/*' : param.includes as String
	} else {
		if (param.remoteFile == null) {
			param_scp.remoteTodir = remotePath(param.remoteDir)
			param_scp.file = param.file as String
		} else {
			param_scp.remoteTofile = remotePath(param.remoteFile)
			param_scp.localFile = param.file as String
		}
	}
	
	if (param.keyfile != null) {
		param_scp.keyfile = param.keyfile as String
		param_scp.passphrase = (param.passphrase == null) ? '' : param.passphrase as String
	}
	
	param_scp.verbose = (param.verbose == null) ? 'no' : param.verbose as String
	param_scp.trust = 'yes'
	param_scp.sftp = 'true'
	
	_guarded {
		if (param_fileset) {
			ant().scp(param_scp) {
				fileset(param_fileset)
			}
		} else {
			ant().scp(param_scp)
		}
	}
}

def ant() {
	return new AntBuilder()
}
