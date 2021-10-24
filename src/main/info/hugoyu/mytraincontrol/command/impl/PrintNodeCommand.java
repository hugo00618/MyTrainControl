package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.LayoutUtil;

public class PrintNodeCommand implements Command {

    @Override
    public boolean execute(String[] args) {
        int nodeId;
        try {
            nodeId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        System.out.println("Owners: ");
        LayoutUtil.getNode(nodeId).getOwnerSummary()
                .forEach((ownerId, summary) -> System.out.println(String.format("%d: %s", ownerId, summary)));

        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"nodeId"};
    }
}
