package org.mifosplatform.logistics.itemdetails.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.logistics.itemdetails.service.ItemDetailsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeAllocateItemCommandHandler implements NewCommandSourceHandler{

	private ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService;
	
	
	@Autowired
	public DeAllocateItemCommandHandler(final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService){
		this.inventoryItemDetailsWritePlatformService = inventoryItemDetailsWritePlatformService;
	}


	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return inventoryItemDetailsWritePlatformService.deAllocateHardware(command);
	}*/

}
