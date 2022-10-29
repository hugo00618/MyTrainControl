package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.LayoutUtil;

public class PrintNodeCommand implements Command {

    @Override
    public void execute(String[] args) {
        int nodeId = Integer.parseInt(args[1]);

        System.out.println("Owners: ");
        LayoutUtil.getNode(nodeId).getOwnerSummary()
                .forEach((ownerId, summary) -> System.out.println(String.format("%d: %s", ownerId, summary)));
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"nodeId"};
    }
}
