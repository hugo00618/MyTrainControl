package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.util.LayoutUtil;

public class PrintNodeCommand implements Command {

    @Override
    public void execute(String[] args) {
        int nodeId0 = Integer.parseInt(args[1]);
        int nodeId1 = Integer.parseInt(args[2]);

        System.out.println("Owners: ");
        LayoutUtil.getNode(new Vector(nodeId0, nodeId1)).getOwnerSummary()
                .forEach((ownerId, summary) -> System.out.println(String.format("%d: %s", ownerId, summary)));
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"nodeId0", "nodeId1"};
    }
}
