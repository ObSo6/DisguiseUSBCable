package com.obso6.disguiseapp.config;

import java.util.ArrayList;

public class Script {
    private ArrayList<Command> cmdList;
    private int iDefaultDelay;

    //构造器
    public Script(ArrayList<Command> cmdList, int defaultDelay) {
        this.cmdList = cmdList;
        iDefaultDelay = defaultDelay;
    }

    public Script() {
        cmdList = new ArrayList<>();
        iDefaultDelay = 500;
    }

    public Script(String sPayload){
        iDefaultDelay = 100;
        readInCommandList(sPayload);
    }

    //String转换为命令，并设置默认延迟
    public void readInCommandList(String sPayload){
        cmdList = new ArrayList<>();
        String[] lines;
        String delimiter = "\n";
        lines = sPayload.split(delimiter);

        //每行加入延时
        for(String s: lines){
            if(s.trim() !="" && s.length()>0 ) {

                //Create command object
                Command c = new Command(s);

                //Check if default delay
                if (c.cmdType == CommandType.DEFAULT_DELAY) {
                    iDefaultDelay = c.getCmdInt();
                }

                //Check if it is a replay command
                if (c.cmdType == CommandType.REPLAY) {

                    //note not added if it is a replay command as it is saved in the previous command.
                    int previousIndex = cmdList.size() - 1;
                    Command previousCommand = cmdList.get(previousIndex);
                    previousCommand.setNumberOfTimesToExecute(c.getCmdInt());
                    cmdList.set(previousIndex, previousCommand);

                } else {
                    //Add it to the list
                    cmdList.add(c);
                }
            }
        }

    }

    public int getDefaultDelay() {
        return iDefaultDelay;
    }

    public void setDefaultDelay(int defaultDelay) {
        iDefaultDelay = defaultDelay;
    }

    public ArrayList<Command> getCmdList() {
        return cmdList;
    }

    public void setCmdList(ArrayList<Command> cmdList) {
        this.cmdList = cmdList;
    }
}
