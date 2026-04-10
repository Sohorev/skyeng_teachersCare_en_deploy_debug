theme: /TeachingAtPlatform

    state: PlatformQuestions
        q!: using the platform
        a: a24.000.004en
        buttons:
            "b24.007.001en"
        scriptEs6: $session.mainMenu1 = true;
        q: back || toState = "/BackBtnTransition"
        
    state: HomeworkAndTests
        q: homework tests and other
        a: a24.000.004en
        buttons:
            "b24.007.002en"
        q: back || toState = "/BackBtnTransition"
        
        state: Tests
            intent!: /platform_tools_tests
            q: tests
            a: a24.007.001en
        
        state: Dictionaries
            intent!: /platform_tools_dictionary
            q: wordlist
            a: a24.007.002en
                
        state: Homework
            intent!: /platform_tools_homework
            q: homework
            a: a24.007.003en
                
        state: LessonRecordings
            intent!: /platform_tools_recordings
            q: lesson recordings
            a: a24.007.004en
                
    state: GroupMeetings
        intent!: /platform_meetings
        q: group meetings for kids and adults
        a: a24.007.005en