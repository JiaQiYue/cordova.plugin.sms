var exec = require('cordova/exec');

var send = module.exports;

send.sendMessage = function (args, success, error) {
    cordova.exec(success, error, 'send', 'sendMessage', args);
};
