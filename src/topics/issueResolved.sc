theme: /IssueResolved
    
    state: Ask
        q!: (thanks/thank you) : thanks
        q!: * [thanks/thank you] no [more] (question*/issue*/problem*) * : thanks
        q!: (goodbye/bye) : bye
        if: $parseTree._Root === "thanks"
            a: a24.000.010en
        elseif: $parseTree._Root === "bye"
            a: a24.000.011en
        a: a24.000.012en
        if: $session.isOperatorOffered
            buttons:
                "b24.000.011en"
        elseif: $session.addButtons
            buttons:
                "b24.000.002en"
        else: 
            buttons:
                "b24.000.012en"
        scriptEs6: 
            $session.beforeTimedOut = $session.statesArray[$session.statesArray.length - 1];
            $session.goToMainMenu = true
            delete $session.statesArray;
            delete $session.addButtons;
            delete $session.isOperatorOffered;
        q: yes * {[issue] * [resolved]} || toState = "/CSI/AskAssessment"
        q: contact the operator || toState = "/SwitchToOperator" 
            
        state: No
            q: no {[issue] * [not resolved]}
            scriptEs6: 
                $session.ppr = false;
                $session.goToMainMenu = true
            a: a24.000.013en
            buttons:
                "b24.000.001en"
            q: main menu || toState = "/OpenWidget"
            q: contact the operator || toState = "/SwitchToOperator"