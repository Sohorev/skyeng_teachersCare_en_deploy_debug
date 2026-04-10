theme: /CSI
    
    state: AskAssessment
        a: a24.000.014en
        scriptEs6: $session.ppr = true;
        buttons:
            "b24.000.014en"

        state: GetAssessment
            q: 5 [wonderful] * :5
            q: 4 [good] * :4
            q: 3 [average] * :3
            q: 2 [poor] * :2
            q: 1 [very bad] * :1
            scriptEs6: $session.score = +$parseTree._Root;
            a: a24.000.015en
            buttons:
                "b24.000.017en"
            go!: /ScenarioStopSession
