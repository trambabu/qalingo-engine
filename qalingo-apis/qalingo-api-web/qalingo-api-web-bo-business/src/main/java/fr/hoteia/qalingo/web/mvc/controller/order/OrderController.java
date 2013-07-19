/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.7.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2013
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package fr.hoteia.qalingo.web.mvc.controller.order;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import fr.hoteia.qalingo.core.BoPageConstants;
import fr.hoteia.qalingo.core.Constants;
import fr.hoteia.qalingo.core.ModelConstants;
import fr.hoteia.qalingo.core.RequestConstants;
import fr.hoteia.qalingo.core.domain.Localization;
import fr.hoteia.qalingo.core.domain.Order;
import fr.hoteia.qalingo.core.i18n.BoMessageKey;
import fr.hoteia.qalingo.core.i18n.enumtype.ScopeWebMessage;
import fr.hoteia.qalingo.core.service.OrderService;
import fr.hoteia.qalingo.core.web.servlet.ModelAndViewThemeDevice;
import fr.hoteia.qalingo.web.mvc.controller.AbstractBusinessBackofficeController;
import fr.hoteia.qalingo.web.mvc.form.OrderForm;
import fr.hoteia.qalingo.web.mvc.viewbean.OrderViewBean;

/**
 * 
 */
@Controller
public class OrderController extends AbstractBusinessBackofficeController {

	@Autowired
	private OrderService orderService;
	
	@RequestMapping(value = "/order-list.html*", method = RequestMethod.GET)
	public ModelAndView orderList(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		ModelAndViewThemeDevice modelAndView = new ModelAndViewThemeDevice(getCurrentVelocityPath(request), BoPageConstants.ORDER_LIST_VELOCITY_PAGE);
		
		final String contentText = getSpecificMessage(ScopeWebMessage.ORDER, BoMessageKey.MAIN_CONTENT_TEXT, getCurrentLocale(request));
		modelAndView.addObject(ModelConstants.CONTENT_TEXT, contentText);
		
		String url = request.getRequestURI();
		String page = request.getParameter(Constants.PAGE_PARAMETER);
		String sessionKey = "PagedListHolder_Orders";
		
		PagedListHolder<OrderViewBean> orderViewBeanPagedListHolder = new PagedListHolder<OrderViewBean>();
		
        if(StringUtils.isEmpty(page)){
        	orderViewBeanPagedListHolder = initList(request, sessionKey);
    		
        } else {
        	orderViewBeanPagedListHolder = (PagedListHolder) request.getSession().getAttribute(sessionKey); 
	        if (orderViewBeanPagedListHolder == null) { 
	        	orderViewBeanPagedListHolder = initList(request, sessionKey);
	        }
	        int pageTarget = new Integer(page).intValue() - 1;
	        int pageCurrent = orderViewBeanPagedListHolder.getPage();
	        if (pageCurrent < pageTarget) { 
	        	for (int i = pageCurrent; i < pageTarget; i++) {
	        		orderViewBeanPagedListHolder.nextPage(); 
				}
	        } else if (pageCurrent > pageTarget) { 
	        	for (int i = pageTarget; i < pageCurrent; i++) {
	        		orderViewBeanPagedListHolder.previousPage(); 
				}
	        } 
        }
		modelAndView.addObject(Constants.PAGE_URL, url);
		modelAndView.addObject(Constants.PAGE_PAGED_LIST_HOLDER, orderViewBeanPagedListHolder);
		
        return modelAndView;
	}
	
	@RequestMapping(value = "/order-details.html*", method = RequestMethod.GET)
	public ModelAndView orderDetails(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		ModelAndViewThemeDevice modelAndView = new ModelAndViewThemeDevice(getCurrentVelocityPath(request), BoPageConstants.ORDER_DETAILS_VELOCITY_PAGE);

		final String currentOrderCode = request.getParameter(RequestConstants.REQUEST_PARAM_ORDER_CODE);
		final Order order = orderService.getOrderById(currentOrderCode);
		
		if(order != null){
			initOrderDetailsPage(request, response, modelAndView, order);
		} else {
			final String url = requestUtil.getLastRequestUrl(request);
			return new ModelAndView(new RedirectView(url));
		}
		
        return modelAndView;
	}
	
	@RequestMapping(value = "/order-edit.html*", method = RequestMethod.GET)
	public ModelAndView orderEdit(final HttpServletRequest request, final HttpServletResponse response, ModelMap modelMap) throws Exception {
		ModelAndViewThemeDevice modelAndView = new ModelAndViewThemeDevice(getCurrentVelocityPath(request), BoPageConstants.ORDER_FORM_VELOCITY_PAGE);
		
		// "order.edit";

		final String currentOrderCode = request.getParameter(RequestConstants.REQUEST_PARAM_ORDER_CODE);
		final Order order = orderService.getOrderByCode(currentOrderCode);
		
		final Localization currentLocalization = requestUtil.getCurrentLocalization(request);
		modelAndView.addObject(Constants.ORDER_VIEW_BEAN, viewBeanFactory.buildOrderViewBean(request, currentLocalization, order));
		modelAndView.addObject(Constants.ORDER_FORM, formFactory.buildOrderForm(request, order));
		return modelAndView;
	}
	
	@RequestMapping(value = "/order-edit.html*", method = RequestMethod.POST)
	public ModelAndView orderEdit(final HttpServletRequest request, final HttpServletResponse response, @Valid OrderForm orderForm,
								BindingResult result, ModelMap modelMap) throws Exception {

		// "order.edit";
		
		final String currentOrderCode = request.getParameter(RequestConstants.REQUEST_PARAM_ORDER_CODE);
		final Order order = orderService.getOrderByCode(currentOrderCode);
		
		if (result.hasErrors()) {
			ModelAndViewThemeDevice modelAndView = new ModelAndViewThemeDevice(getCurrentVelocityPath(request), BoPageConstants.ORDER_FORM_VELOCITY_PAGE);
			final Localization currentLocalization = requestUtil.getCurrentLocalization(request);
			modelAndView.addObject(Constants.ORDER_VIEW_BEAN, viewBeanFactory.buildOrderViewBean(request, currentLocalization, order));
			modelAndView.addObject(Constants.ORDER_FORM, formFactory.buildOrderForm(request, order));
			return modelAndView;
		}
		
		// UPDATE ORDER
//		webBackofficeService.updateOrder(order, orderForm);
		
		final String urlRedirect = backofficeUrlService.buildOrderDetailsUrl(currentOrderCode);
        return new ModelAndView(new RedirectView(urlRedirect));
	}

	private PagedListHolder<OrderViewBean> initList(final HttpServletRequest request, String sessionKey) throws Exception{
		final Localization currentLocalization = requestUtil.getCurrentLocalization(request);
		PagedListHolder<OrderViewBean> orderViewBeanPagedListHolder = new PagedListHolder<OrderViewBean>();
		
		final List<OrderViewBean> orderViewBeans = new ArrayList<OrderViewBean>();

		final List<Order> orders = orderService.findOrders();
		for (Iterator<Order> iterator = orders.iterator(); iterator.hasNext();) {
			Order order = (Order) iterator.next();
			orderViewBeans.add(viewBeanFactory.buildOrderViewBean(request, currentLocalization, order));
		}
		orderViewBeanPagedListHolder = new PagedListHolder<OrderViewBean>(orderViewBeans);
		orderViewBeanPagedListHolder.setPageSize(Constants.PAGE_SIZE);
        request.getSession().setAttribute(sessionKey, orderViewBeanPagedListHolder); 
        return orderViewBeanPagedListHolder;
	}
    
	protected void initOrderDetailsPage(final HttpServletRequest request, final HttpServletResponse response, final ModelAndViewThemeDevice modelAndView, final Order user) throws Exception{
		final Localization currentLocalization = requestUtil.getCurrentLocalization(request);
		// "order.details";

		modelAndView.addObject(Constants.ORDER_VIEW_BEAN, viewBeanFactory.buildOrderViewBean(request, currentLocalization, user));
	}
}