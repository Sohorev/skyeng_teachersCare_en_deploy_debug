require: import.sc

init: 
    
    bind("onAnyError", function($context) {
        var date = $jsapi.dateForZone("Europe/Moscow", "dd.MM.YYYY HH:mm:ss");
        var data = DiBugBot.configureMessage($context, $injector.bugBot.botName, date);
        if ($context.exception) {
            $context.exception.message = $context.exception.message || $context.exception.type;
            $reactions.answer($context.exception.message);
        }
        # DiBugBot.onAnyError($injector.bugBot, date, $context.exception.message, data);
        # $.session.sessionResult = "error occurred";
        # switchToOperator();
    });
    
    bind("preProcess", function($context) {
        if (!$context.session) $context.session = {};
        if (!$context.temp) $context.temp = {};
        if (!$context.client) $context.client = {};
        // массив стейтов для перехода по кнопке "Back"
        if (!$.session.statesArray) {
            $.session.statesArray = ["/OpenWidget", ($.currentState === "/NoMatch") ? "/MainMenu" : $.currentState];
        } else if (!_.includes([$.session.statesArray[$.session.statesArray.length - 1], "/BackBtnTransition"], $.currentState)) {
            $.session.statesArray.push(($.currentState === "/NoMatch") ? "/MainMenu" : $.currentState);
        }
        // для аналитики bounce rate
        if ($.session.isBounceRate
            && $.temp.classifierTargetState
            && !(_.includes(["/OpenWidget", "/AskSwitchToOperator"], $.temp.classifierTargetState))
            && !$.temp.classifierTargetState.startsWith("/TestPhrases")) $.session.isBounceRate = false;
        // сессионные данные
        $context.session = sessionObjectUpdate($.session, $.request.data);
        // если второй раз подряд в этом стейте, переход на оператора
        $context = sameStateTwice($context);
        // для аналитики query count
        if (!$.session.lastState && isContactToday($.client.lastTime)) $.client.queryCounter = ++$.client.queryCounter || 2;
        else if (!$.session.lastState) {
            $.client.queryCounter = 1;
            $.client.lastTime = moment($jsapi.dateForZone("Europe/Moscow", "yyyy-MM-dd hh:mm:ss"));
        }
        
        if ($.session.afterNoMatch && $.currentState !== "/NoMatch")
            delete $.session.afterNoMatch;
        if ($.session.afterIncorrectID && $.currentState !== "/RecoverStudentAfterBreak/AskForStudentID/LocalCatchAll")
            delete $.session.afterIncorrectID;
        
    });
    
    $jsapi.bind({
        type: "postProcess",
        name: "forThematic",
        path: "/",
        handler: function($context) {
            var m = (typeof $global !== "undefined" && $global.teachersCareMainPostProcess)
                || (typeof teachersCareMainPostProcess !== "undefined" && teachersCareMainPostProcess);
            return m.forThematicPostProcess();
        }
    });

    bind("postProcess", function($context) {
        $.response.replies = _.filter($.response.replies, function(reply) { return reply.type !== "timeout"; });
        if (!_.includes(["/SwitchToOperator", "/ScenarioStopSession",
                "/StopSession", "/SwitchToOperator/Forced",
                "/TestPhrases/Reset", "/LimitErrors"], $.currentState)) {
            var interval = $.session.timeoutInterval || $.injector.timeoutInterval;
            $reactions.timeout({interval: interval, targetState: "/TimedOut"});
        }
        if (!_.includes(["/ScenarioStopSession", "/SwitchOper", "/TestPhrases/Reset",
            "/LimitErrors", "/SwitchOper/Forced"], $.currentState)) $.session.lastState = $.currentState;
    });
    
    
    $global.answerId = /a[0-9]{2}\.[0-9]{3}\.[0-9]{3}(?:ru|en)/;
    $global.buttonId = /b[0-9]{2}\.[0-9]{3}\.[0-9]{3}(?:ru|en)/;
    
    $jsapi.bind({
        type: "postProcess",
        name: "answerChanger",
        path: "/",
        handler: function($context) {
            var m = (typeof $global !== "undefined" && $global.teachersCareMainPostProcess)
                || (typeof teachersCareMainPostProcess !== "undefined" && teachersCareMainPostProcess);
            return m.answerChangerPostProcess();
        }
    });

theme: /

    state: Start
        event!: newSession
        q!: *start
        scriptEs6:
            $jsapi.startSession();
            $context.session = sessionObjectUpdate($.session, $.request.rawRequest);
            $session.isBounceRate = true;
        a: a24.000.001en
        if: $injector.infoMsg && !testMode()
            a: a24.000.002en
            
    state: OpenWidget
        event!: OpenWidget
        q!: (hi/hello)
        q!: main menu
        q!: [i] need [your] help
        scriptEs6: 
            $session.mainMenu1 = true;
            if ($session.isBounceRate !== "boolean") $session.isBounceRate = true;
            if ($.intent && $.intent.path === "/help") $session.isBounceRate = false;
            if (!$session.lessonNow || $session.goToMainMenu) {
                $reactions.transition("/MainMenu");
            } else {
                $session.goToMainMenu = true;
                $reactions.answer("a24.000.004en");
                $reactions.buttons("b24.000.001en");
            }
        q: main menu || toState = "/MainMenu"
        q: contact the operator || toState = "/SwitchToOperator"
    
    state: NoMatch
        event!: noMatch
        q!: back
        scriptEs6: 
            if ($session.lastState === "/NoMatch" || $session.afterNoMatch) {
                $session.noMatchCount = ++$session.noMatchCount || 1;
            } else delete $session.noMatchCount;
            
            if (!$session.noMatchCount) {
                $reactions.answer("a24.000.009en");
                $session.afterNoMatch = true;
                delete $session.mainMenu1;
                $reactions.transition("/MainMenu");
            } else if ($session.noMatchCount === 1) {
                $reactions.answer("a24.000.008en");
                $reactions.buttons("b24.000.001en");
            } else {
                $session.sessionRes = "switch to operator_na";
                $reactions.transition("/SwitchToOperator");
            };
        
    state: AlreadyAsked
        q!: * {[asked] * question (already/above/previously)} * 
        q!: * {(asked) [question] (already/above/previously)} *
        a: a24.000.003en
        go!: /MainMenu
    
    state: AskSwitchToOperator    
        q!: contact [the/with] (operator/human)
        q!: operator
        q!: * want [to] (talk [to]/ask) [the/an/with/for] (operator/human)
        scriptEs6:
            // для bounce rate
            if (!_.includes(_.keys($session), "bounceRate")) $session.bounceRate = $session.isBounceRate;
            else if (!_.includes(_.keys($session), "isBounceRate")
                && !_.includes(_.keys($session), "bounceRate") && 
                !$session.lastState) $session.bounceRate = true;
            // если есть урок или была кнопка b18.510
            if ($session.lessonNow || $session.lastState === "/NoMatch") {
                $session.sessionRes = "switch to operator_st";
                $reactions.transition("/SwitchToOperator");
            } else {
                $reactions.answer("a24.000.005en");
                $reactions.transition("/MainMenu");
            }
        
    state: SwitchToOperator || noContext = true
        event!: fileEvent
        q!: * {([for] [a] long time) * (no answer*)} *
        q!: {(no answer) (for so long) [from you]}
        a: a24.000.006en
        scriptEs6:
            var currentScenario = stateToIntent[$.contextPath];
            if ($.contextPath) {
                $.session.scenarios.push(currentScenario);
                if (isThematic($.contextPath) && !$temp.twiceRequest) await formResponseDataAsync.setCommentAsync($session, currentScenario, $request.query, true);
            }
            switchToOperator(currentScenario);
            
        state: Forced
            event!: forceSwitchOperator
            scriptEs6: switchToOperator();

    state: LimitErrors
        event!: lengthLimit
        event!: timeLimit
        a: a24.000.006en
        scriptEs6:
            $session.sessionRes = "error occured";
            switchToOperator();
    
    state: TimedOut
        scriptEs6: 
            $session.mainMenu1 = true;
            $session.beforeTimedOut = $session.beforeTimedOut || $session.lastState;
            $temp.isTimeout = true;
        if: $session.lastState !== "/MainMenu"
            go!: /MainMenu
        go!: /ScenarioStopSession
    
    state: ScenarioStopSession
        scriptEs6:
            $analytics.setSessionResult($temp.isTimeout ? "session timeout" : "completed scenario");
            $response.data = await formResponseDataAsync.buildFormResponseData($session, $request.query, stateToIntent[$.currentState]);
            closeSession();
    
    state: BackBtnTransition
        scriptEs6:
            var backBtnTargetState = backBtnTransition($session.statesArray);
            $session.statesArray = upgradeStatesArray($session.statesArray);
            $reactions.transition(backBtnTargetState);
        
    state: MainMenu
        if: $session.mainMenu1
            a: a24.000.004en
            scriptEs6: delete $session.mainMenu1;
        buttons:
            "b24.000.015en"
        
        state: VirtualAssistans
            q: interaction with the Skyeng Virtual Assistant
            a: a24.000.016en
            go!: /IssueResolved/Ask
            
        state: PopularQuestions
            q!: popular questions
            a: a24.000.004en
            scriptEs6: $session.mainMenu1 = true;
            buttons:
                "b24.000.016en"
            q: back || toState = "/BackBtnTransition"
            
            state: SaveLesson
                intent!: /lesson_save
                q: saving a lesson
                a: a24.000.017en
                go!: /IssueResolved/Ask
                
