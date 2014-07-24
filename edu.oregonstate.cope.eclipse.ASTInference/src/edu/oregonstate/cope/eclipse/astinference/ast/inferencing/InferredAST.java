package edu.oregonstate.cope.eclipse.astinference.ast.inferencing;

import org.json.simple.JSONArray;

/**
 * Created by michaelhilton on 7/21/14.
 */
public class InferredAST {
        private JSONArray inferredAST ;
        public InferredAST(){
        }
        public void setInferredAST(JSONArray jsonArr){
            inferredAST = jsonArr;
        }
        public JSONArray getInferredAST(){
            return inferredAST;
        }
    }

