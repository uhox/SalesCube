INSERT INTO ONLINE_ORDER_REL_/*$domainId*/
(
	RO_SLIP_ID,
	RO_LINE_ID,
	ONLINE_ORDER_ID,
	ONLINE_ITEM_ID,
	CRE_FUNC,
	CRE_DATETM,
	CRE_USER,
	UPD_FUNC,
	UPD_DATETM,
	UPD_USER
)
VALUES
(
	/*roSlipId*/,
	/*roLineId*/,
	/*onlineOrderId*/,
	/*onlineItemId*/,
	/*creFunc*/,
	now(),
	/*creUser*/,
	/*updFunc*/,
	now(),
	/*updUser*/
)