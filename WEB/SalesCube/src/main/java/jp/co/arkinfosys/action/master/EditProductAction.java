/*
 *  Copyright 2009-2010 Ark Information Systems.
 */

package jp.co.arkinfosys.action.master;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import jp.co.arkinfosys.common.Categories;
import jp.co.arkinfosys.common.Constants;
import jp.co.arkinfosys.common.MessageResourcesUtil;
import jp.co.arkinfosys.common.StringUtil;
import jp.co.arkinfosys.dto.master.ProductDto;
import jp.co.arkinfosys.entity.AuditInfo;
import jp.co.arkinfosys.entity.Discount;
import jp.co.arkinfosys.entity.Product;
import jp.co.arkinfosys.entity.ProductClass;
import jp.co.arkinfosys.entity.Rack;
import jp.co.arkinfosys.entity.join.ProductJoin;
import jp.co.arkinfosys.entity.join.SupplierJoin;
import jp.co.arkinfosys.form.master.AbstractEditForm;
import jp.co.arkinfosys.form.master.EditProductForm;
import jp.co.arkinfosys.service.AbstractMasterEditService;
import jp.co.arkinfosys.service.CategoryService;
import jp.co.arkinfosys.service.DiscountService;
import jp.co.arkinfosys.service.InitMstService;
import jp.co.arkinfosys.service.ProductClassService;
import jp.co.arkinfosys.service.ProductService;
import jp.co.arkinfosys.service.RackService;
import jp.co.arkinfosys.service.SupplierService;
import jp.co.arkinfosys.service.exception.ServiceException;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.seasar.framework.beans.util.Beans;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;
import org.seasar.struts.util.ActionMessagesUtil;

/**
 * 商品編集画面のアクションクラスです.
 * @author Ark Information Systems
 *
 */
public class EditProductAction extends
		AbstractEditAction<ProductDto, ProductJoin> {

	/**
	 * 画面遷移用のマッピングクラス
	 */
	protected static class Mapping {
		public static final String INPUT = "editProduct.jsp";
	}

	@ActionForm
	@Resource
	public EditProductForm editProductForm;

	@Resource
	private ProductService productService;

	@Resource
	private CategoryService categoryService;

	@Resource
	private ProductClassService productClassService;

	@Resource
	private InitMstService initMstService;

	@Resource
	private SupplierService supplierService;

	@Resource
	private RackService rackService;

	@Resource
	private DiscountService discountService;

	/**
	 * 数量割引コードが変更されたか否か
	 */
	private boolean discountIdChanged = false;

	/**
	 * 新規登録時の初期化処理を行います.<br>
	 * 処理終了後、{@link Mapping#INPUT}で定義されたURIに遷移します.
	 * @return 画面遷移先のURI文字列
	 * @throws Exception
	 */
	@Execute(validator = false)
	public String index() throws Exception {
		
		this.init(null);
		return EditProductAction.Mapping.INPUT;
	}

	/**
	 * 既存データを新規追加モードで開きます.<BR>
	 * 処理終了後、{@link EditProductAction#getInputURL()}で取得したURIに遷移します.
	 * @return 画面遷移先のURI文字列
	 * @throws Exception
	 */
	@Execute(validator = false)
	public String load() throws Exception {
		super.doEdit(this.getKey());

		
		this.editProductForm.productCode = null;
		this.editProductForm.editMode = false;
		this.editProductForm.avgShipCount = null;
		this.editProductForm.creDatetm = null;
		this.editProductForm.creDatetmShow = null;
		this.editProductForm.updDatetm = null;
		this.editProductForm.updDatetmShow = null;
		this.editProductForm.discountUpdDatetm = null;

		return this.getInputURL();
	}

	/**
	 * 編集モード時の初期化処理を行います.<br>
	 * 処理終了後、{@link EditProductAction#getInputURL()}で取得したURIに遷移します.
	 *
	 * @return 画面遷移先のURI文字列
	 * @throws Exception
	 */
	@Execute(validator = false)
	public String edit() throws Exception {
		return super.doEdit(this.getKey());
	}

	/**
	 * 登録処理を行います.<br>
	 * 処理実行後、{@link EditProductAction#getInputURL()}で取得したURIに遷移します.
	 * @return 画面遷移先のURI文字列
	 * @throws Exception
	 */
	@Execute(validator = true, validate = "validate, validateCode", input = EditProductAction.Mapping.INPUT, stopOnValidationError = false)
	public String insert() throws Exception {
		return super.doInsert();
	}

	/**
	 * 更新処理を行います.<br>
	 * 処理終了後、{@link EditSupplierAction#doUpdate()}で取得したURIに遷移します.
	 * @return 画面遷移先のURI文字列
	 * @throws Exception
	 */
	@Execute(validator = true, validate = "validate, validateCode", input = EditProductAction.Mapping.INPUT)
	public String update() throws Exception {
		return this.doUpdate();
	}

	/**
	 * 更新処理を行います.<br>
	 * 更新完了時およびロック失敗時、画面にメッセージを表示します.<br>
	 * 処理実行後、{@link EditProductAction#getInputURL()}で取得したURIに遷移します.
	 * @return 画面遷移先のURI文字列
	 * @throws Exception
	 * @throws ServiceException
	 */
	@Override
	protected String doUpdate() throws Exception, ServiceException {
		ProductJoin current = this.productService.findById(this.getKey());
		if (current == null) {
			super.messages.add(ActionMessages.GLOBAL_MESSAGE,
					new ActionMessage("errors.exclusive.control.deleted"));
			ActionMessagesUtil.addErrors(super.httpRequest, super.messages);
			return this.getInputURL();
		}

		ProductJoin productJoin = Beans.createAndCopy(ProductJoin.class,
				this.editProductForm).dateConverter(Constants.FORMAT.DATE)
				.timestampConverter(Constants.FORMAT.TIMESTAMP).execute();

		this.discountIdChanged = !((current.discountId == null && productJoin.discountId == null) || (current.discountId != null
				&& productJoin.discountId != null && current.discountId
				.equals(productJoin.discountId)));

		if (!current.equals(productJoin) || this.discountIdChanged) {
			return super.doUpdate();
		}
		super.messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				"infos.noupdate"));
		ActionMessagesUtil.addMessages(this.httpRequest, super.messages);
		return this.getInputURL();
	}

	/**
	 * {@link EditProductForm}から{@link ProductDto}への値コピーを行います.
	 * @return 値コピー後の{@link ProductDto}
	 */
	@Override
	protected ProductDto createDtoFromActionForm() {
		ProductDto dto = new ProductDto();
		dto.discountIdChanged = this.discountIdChanged;
		Beans.copy(this.editProductForm, dto).dateConverter(
				Constants.FORMAT.DATE).timestampConverter(
				Constants.FORMAT.TIMESTAMP).execute();
		return dto;
	}

	/**
	 * 削除処理を行います.
	 *
	 * @return 画面遷移先のURI文字列
	 * @throws Exception
	 */
	@Execute(validator = false)
	public String delete() throws Exception {
		return this.doDelete();
	}

	/**
	 * 削除処理を行います.<br>
	 * 削除前に関連データの存在チェックを行います.<br>
	 * 処理実行後、{@link EditProductAction#getInputURL()}で取得したURIに遷移します.
	 * @return 画面遷移先のURI文字列
	 * @throws Exception
	 */
	@Override
	protected String doDelete() throws Exception {
		
		Map<String, Object> result = this.productService.countRelations(this
				.getKey());

		Iterator<Entry<String, Object>> ite = result.entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, Object> entry = ite.next();
			Number num = (Number) entry.getValue();
			if (num != null && num.longValue() > 0) {
				super.messages.add(ActionMessages.GLOBAL_MESSAGE,
						new ActionMessage("errors.db.delete.relation",
								MessageResourcesUtil.getMessage("erroes.db."
										+ entry.getKey())));
			}
		}

		if (super.messages.size() > 0) {
			ActionMessagesUtil.addErrors(super.httpRequest, super.messages);
			return this.getInputURL();
		}

		return super.doDelete();
	}

	/**
	 * 基底クラスの初期化処理の拡張です.<BR>
	 * 基底クラスの初期化処理後、端数処理と小数桁処理を行います.
	 * @param key 商品コード
	 * @throws ServiceException
	 */
	@Override
	protected void init(String key) throws ServiceException {
		
		this.editProductForm.lotUpdFlag = Constants.FLAG.ON;
		this.editProductForm.maxPoUpdFlag = Constants.FLAG.ON;
		this.editProductForm.poUpdFlag = Constants.FLAG.ON;
		this.editProductForm.stockUpdFlag = Constants.FLAG.ON;
		this.editProductForm.mineSafetyStockUpdFlag = Constants.FLAG.ON;

		
		this.initMstService.initBean(Product.TABLE_NAME,
				this.editProductForm);

		super.init(key);

		
		this.editProductForm.fractCategory = super.mineDto.productFractCategory;
		
		this.editProductForm.taxCategory = super.mineDto.taxCategory;
		
		this.editProductForm.productFractCategory = super.mineDto.productFractCategory;
		
		this.editProductForm.numDecAlignment = String
				.valueOf(super.mineDto.numDecAlignment);
		
		this.editProductForm.priceFractCategory = super.mineDto.priceFractCategory;
		
		this.editProductForm.unitPriceDecAlignment = "0";
		
		this.editProductForm.dolUnitPriceDecAlignment = String
				.valueOf(super.mineDto.unitPriceDecAlignment);
		
		this.editProductForm.statsDecAlignment = String
				.valueOf(super.mineDto.statsDecAlignment);
	}

	/**
	 *
	 * @param key 商品コード
	 * @return {@link ProductJoin}
	 * @throws ServiceException
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#loadData(java.lang.String)
	 */
	@Override
	protected AuditInfo loadData(String key) throws ServiceException {
		return this.productService.findById(key);
	}

	/**
	 *
	 * @param record {@link ProductJoin}
	 * @throws ServiceException
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#setForm(jp.co.arkinfosys.entity.AuditInfo)
	 */
	@Override
	protected void setForm(AuditInfo record) throws ServiceException {
		super.setForm(record);

		if (record != null) {
			ProductJoin productJoin = (ProductJoin) record;

			
			if (StringUtil.hasLength(productJoin.supplierCode)) {
				SupplierJoin supplierJoin = this.supplierService
						.findById(productJoin.supplierCode);
				if (supplierJoin != null) {
					
					this.editProductForm.priceFractCategory = supplierJoin.priceFractCategory;
					
					this.editProductForm.unitPriceDecAlignment = supplierJoin.unitPriceDecAlignment;
					
					this.editProductForm.dolUnitPriceDecAlignment = supplierJoin.dolUnitPriceDecAlignment;

					if (supplierJoin.rateId != null) {
						SupplierJoin supplier = supplierService
								.findSupplierRateByCodeDate(
										productJoin.supplierCode,
										new java.sql.Date(productJoin.updDatetm
												.getTime()));
						if (supplier != null) {
							
							this.editProductForm.supplierRate = supplier.supplierRate;
						}
					}
				}
			}

			
			if (StringUtil.hasLength(this.editProductForm.product2)) {
				this.editProductForm.product2List
						.addAll(this.productClassService
								.findAllProductClass2LabelValueBeanList(this.editProductForm.product1));

				
				if (StringUtil.hasLength(this.editProductForm.product3)) {
					Map<String, Object> conditions = new HashMap<String, Object>();
					conditions.put(ProductClassService.Param.CLASS_CODE_1,
							this.editProductForm.product1);
					conditions.put(ProductClassService.Param.CLASS_CODE_2,
							this.editProductForm.product2);

					List<ProductClass> productClassList = this.productClassService
							.findProductClassByCondition(conditions);
					for (ProductClass productClass : productClassList) {
						this.editProductForm.product3List
								.add(new LabelValueBean(productClass.className,
										productClass.classCode3));
					}
				}
			}
		}
	}

	/**
	 * プルダウン要素を作成します.
	 * @throws ServiceException
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#initList()
	 */
	protected void initList() throws ServiceException {
		// 
		
		this.editProductForm.stockCtlCategoryList = this.categoryService
				.findCategoryLabelValueBeanListById(Categories.PRODUCT_STOCK_CTL);

		
		this.editProductForm.setTypeCategoryList = this.categoryService
				.findCategoryLabelValueBeanListById(Categories.PRODUCT_SET_TYPE);
		this.editProductForm.setTypeCategoryList.add(0, new LabelValueBean());

		
		this.editProductForm.standardCategoryList = this.categoryService
				.findCategoryLabelValueBeanListById(Categories.PRODUCT_STANDARD);
		this.editProductForm.standardCategoryList.add(0, new LabelValueBean());

		
		this.editProductForm.statusCategoryList = this.categoryService
				.findCategoryLabelValueBeanListById(Categories.PRODUCT_STATUS);
		this.editProductForm.statusCategoryList.add(0, new LabelValueBean());

		
		this.editProductForm.stockCategoryList = this.categoryService
				.findCategoryLabelValueBeanListById(Categories.PRODUCT_STOCK);
		this.editProductForm.stockCategoryList.add(0, new LabelValueBean());

		
		this.editProductForm.purvayCategoryList = this.categoryService
				.findCategoryLabelValueBeanListById(Categories.PRODUCT_PURVAY);
		this.editProductForm.purvayCategoryList.add(0, new LabelValueBean());

		
		this.editProductForm.unitList = this.categoryService
				.findCategoryLabelValueBeanListById(Categories.PRODUCT_UNIT);
		this.editProductForm.unitList.add(0, new LabelValueBean());

		
		this.editProductForm.weightUnitList = this.categoryService
				.findCategoryLabelValueBeanListById(Categories.PRODUCT_UNIT_WEIGHT);
		this.editProductForm.weightUnitList.add(0, new LabelValueBean());

		
		this.editProductForm.lengthUnitList = this.categoryService
				.findCategoryLabelValueBeanListById(Categories.PRODUCT_UNIT_SIZE);
		this.editProductForm.lengthUnitList.add(0, new LabelValueBean());

		
		this.editProductForm.product1List = this.productClassService
				.findAllProductClass1LabelValueBeanList();
		this.editProductForm.product1List.add(0, new LabelValueBean());

		this.editProductForm.product2List.add(0, new LabelValueBean());
		this.editProductForm.product3List.add(0, new LabelValueBean());
	}

	/**
	 * 伝票登録時のバリデートを行います.
	 *
	 * @return 表示するメッセージ
	 * @throws ServiceException
	 */
	public ActionMessages validateCode() throws ServiceException {
		ActionMessages codeErrors = new ActionMessages();
		
		if (StringUtil.hasLength(editProductForm.supplierCode)) {
			SupplierJoin supplierJoin = this.supplierService
					.findById(editProductForm.supplierCode);
			if (supplierJoin == null) {
				codeErrors.add(ActionMessages.GLOBAL_MESSAGE,
						new ActionMessage("errors.notExist",
								MessageResourcesUtil
										.getMessage("labels.supplierCode")));
			}
		}

		
		if (StringUtil.hasLength(editProductForm.rackCode)) {
			Rack rack = this.rackService.findById(editProductForm.rackCode);
			if (rack == null) {
				codeErrors.add(ActionMessages.GLOBAL_MESSAGE,
						new ActionMessage("errors.notExist",
								MessageResourcesUtil
										.getMessage("labels.rackCode")));
			} else {
				if (!this.rackService.checkPossibleRack(
						editProductForm.productCode, editProductForm.rackCode)) {
					codeErrors.add(ActionMessages.GLOBAL_MESSAGE,
							new ActionMessage("errors.rackNoEmpty"));
				}
			}
		}

		
		if (StringUtil.hasLength(editProductForm.discountId)) {
			Discount discount = this.discountService
					.findById(editProductForm.discountId);
			if (discount == null) {
				codeErrors.add(ActionMessages.GLOBAL_MESSAGE,
						new ActionMessage("errors.notExist",
								MessageResourcesUtil
										.getMessage("labels.discountId")));
			}
		}

		return codeErrors;
	}

	/**
	 *
	 * @return {@link EditProductForm}
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#getActionForm()
	 */
	@Override
	protected AbstractEditForm getActionForm() {
		return this.editProductForm;
	}

	/**
	 *
	 * @return {@link ProductDto}
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#getDtoClass()
	 */
	@Override
	protected Class<ProductDto> getDtoClass() {
		return ProductDto.class;
	}

	/**
	 *
	 * @return {@link Mapping#INPUT}で定義されたURI文字列
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#getInputURL()
	 */
	@Override
	protected String getInputURL() {
		return Mapping.INPUT;
	}

	/**
	 *
	 * @return 商品コード
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#getKey()
	 */
	@Override
	protected String getKey() {
		return this.editProductForm.productCode;
	}

	/**
	 *
	 * @return {@link MENU_ID#MASTER_PRODUCT}
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#getMenuId()
	 */
	@Override
	protected String getMenuId() {
		return Constants.MENU_ID.MASTER_PRODUCT;
	}
	/**
	 *
	 * @return {@link ProductService}
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#getService()
	 */
	@Override
	protected AbstractMasterEditService<ProductDto, ProductJoin> getService() {
		return this.productService;
	}

	/**
	 *
	 * @return　プロパティキー文字列
	 * @see jp.co.arkinfosys.action.master.AbstractEditAction#getAlreadyExistsErrorKey()
	 */
	@Override
	protected String getAlreadyExistsErrorKey() {
		return "errors.product.already.exists";
	}
}
