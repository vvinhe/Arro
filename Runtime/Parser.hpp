//
//  parser.hpp
//  parser
//
//  Created by Gerard Teurlinx on 06/07/16.
//  Copyright © 2016 Gerard Teurlinx. All rights reserved.
//

#ifndef parser_hpp
#define parser_hpp

#include <stdio.h>
#include <map>
#include <set>
#include <list>
#include <string>
#include "Tokenizer.hpp"

class Instruction {
public:
    Instruction(int state1, std::string argument, int state2) {
        m_state1 = state1;
        m_state2 = state2;
        m_argument = argument;
    }
    std::string& getArgument() {
        return m_argument;
    }
    bool match(int s1, int s2) {
        return s1 == m_state1 && s2 == m_state2;
    }
    
private:
    int m_state1;
    int m_state2;
    std::string m_argument;
};

class State {
public:
    State(int initialState) {
        history.push_back(initialState);
    }
    void add(int newState) {
        history.push_back(newState);
    }
    int get() const {
        return history.back();
    }
    void collectInstructions(Tokenizer& tokens, std::list<Instruction>& instructions) {
        tokens.reset();
        
        auto c = history.begin();
        while(c != history.end()) {
            auto c1 = c;
            auto c2 = ++c;
            if(c != history.end()) {
                std::string tmp;
                tokens.getToken(tmp);
                instructions.push_back(Instruction(*c1, tmp, *c2));

                std::cout << "Rule " << *c1 << " - " << tmp << " - " << *c2 << "\n";
            }
        }
    }
    
    
private:
    std::list<int> history;
};

class Parser {
private:
    // multimap allows multiple entries with same key value.
    // Below is a multimap of pair<state, token> --> newstate
    std::multimap<std::pair<int, char>, int> m_transitions;
    std::set<int> m_acceptingStates;
    int m_startState;
    
public:
    Parser(int startState, int endState);
    void addRule(int curState, char token, int newState);
    bool parse(Tokenizer& tokens, std::list<Instruction>& instructions);
};


#endif /* parser_hpp */