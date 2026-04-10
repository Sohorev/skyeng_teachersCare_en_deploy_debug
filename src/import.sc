require: dateTime/dateTime.sc
  module = sys.zb-common
  
require: dateTime/moment.min.js
  module = sys.zb-common

require: scripts/globals.js
    type = scriptEs6
    name = globals

require: scripts/bugbot.js
  module = common_di
  
require: scripts/utils.js
  module = common_di

require: functions/functions.js
  module = skyeng_common

require: functions/utils.js
    module = skyeng_common

require: scripts/formResponseDataAsync.js
    type = scriptEs6
    name = formResponseDataAsync

require: scripts/teachersCareMainPostProcess.js
    type = scriptEs6
    name = teachersCareMainPostProcess

require: topics/teachingAtPlatform.sc
require: topics/issueResolved.sc
require: topics/csi.sc

require: dicts/answers.yaml
    var = answers
    name = answers
    
require: dicts/buttons.yaml
    var = buttons
    name = buttons

require: dicts/stateToIntent.yaml
    var = stateToIntent
    name = stateToIntent
