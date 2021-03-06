/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.8.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2014
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package org.hoteia.qalingo.core.solr.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hibernate.Hibernate;
import org.hoteia.qalingo.core.domain.CatalogCategoryVirtual;
import org.hoteia.qalingo.core.domain.MarketArea;
import org.hoteia.qalingo.core.domain.ProductMarketing;
import org.hoteia.qalingo.core.domain.ProductSku;
import org.hoteia.qalingo.core.domain.ProductSkuOptionDefinition;
import org.hoteia.qalingo.core.domain.ProductSkuOptionRel;
import org.hoteia.qalingo.core.domain.ProductSkuPrice;
import org.hoteia.qalingo.core.domain.Retailer;
import org.hoteia.qalingo.core.service.ProductService;
import org.hoteia.qalingo.core.solr.bean.ProductSkuSolr;
import org.hoteia.qalingo.core.solr.response.ProductSkuResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("productSkuSolrService")
@Transactional
public class ProductSkuSolrService extends AbstractSolrService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Autowired
    public SolrServer productSkuSolrServer;
    
    @Autowired
    protected ProductService productService;
    
    public void addOrUpdateProductSku(final ProductMarketing productMarketing, final ProductSku productSku, final List<CatalogCategoryVirtual> catalogCategories, final MarketArea marketArea, final Retailer retailer) throws SolrServerException, IOException {
        if (productSku.getId() == null) {
            throw new IllegalArgumentException("Id  cannot be blank or null.");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Indexing productSku " + productSku.getId() + " : " + productSku.getCode()+ " : " + productSku.getName());
        }
        ProductSkuSolr productSkuSolr = new ProductSkuSolr();
        productSkuSolr.setId(productSku.getId());
        productSkuSolr.setCode(productSku.getCode());
        productSkuSolr.setName(productSku.getName());
        productSkuSolr.setDescription(productSku.getDescription());

        productSkuSolr.setEnabledToB2C(productSku.isEnabledToB2C());

        if(productMarketing.getProductBrand() != null
                && Hibernate.isInitialized(productMarketing.getProductBrand())){
            productSkuSolr.setProductBrandCode(productMarketing.getProductBrand().getCode());
        }
        
        CatalogCategoryVirtual defaultVirtualCatalogCategory = productService.getDefaultVirtualCatalogCategory(productSku, catalogCategories, true);

        if(defaultVirtualCatalogCategory != null){
            productSkuSolr.setDefaultCategoryCode(defaultVirtualCatalogCategory.getCode());
        }

        if(catalogCategories != null){
            for (CatalogCategoryVirtual catalogCategoryVirtual : catalogCategories) {
                String catalogCode = catalogCategoryVirtual.getCatalog().getCode(); 
                productSkuSolr.addCatalogCode(catalogCode);
                String catalogCategoryCode = catalogCode + "_" + catalogCategoryVirtual.getCode(); 
                productSkuSolr.addCatalogCategories(catalogCategoryCode);
            }
        }
        
        if(productSku.getOptionRels() != null
                && Hibernate.isInitialized(productSku.getOptionRels())
                && !productSku.getOptionRels().isEmpty()){
            for (ProductSkuOptionRel productSkuOptionRel : productSku.getOptionRels()) {
                ProductSkuOptionDefinition productSkuOptionDefinition = productSkuOptionRel.getProductSkuOptionDefinition(); 
                productSkuSolr.addOptionDefinition(productSkuOptionDefinition.getCode());
            }
        }
        
        if(marketArea != null && retailer != null){
            ProductSkuPrice productSkuPrice = productSku.getBestPrice(marketArea.getId());
            if(productSkuPrice != null){
                BigDecimal salePrice = productSkuPrice.getSalePrice();
                productSkuSolr.setPrice(salePrice.toString());
            }
        }
        
        productSkuSolrServer.addBean(productSkuSolr);
        productSkuSolrServer.commit();
    }

    public void removeProductSku(final ProductSkuSolr productSkuSolr) throws SolrServerException, IOException {
        if (productSkuSolr.getId() == null) {
            throw new IllegalArgumentException("Id  cannot be blank or null.");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Remove Index ProductSku " + productSkuSolr.getId() + " : " + productSkuSolr.getCode() + " : " + productSkuSolr.getName());
        }
        productSkuSolrServer.deleteById(productSkuSolr.getId().toString());
        productSkuSolrServer.commit();
    }
    
    public ProductSkuResponseBean searchProductSku(String searchQuery, List<String> facetFields) throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setParam("rows", getMaxResult());
        
        if (StringUtils.isEmpty(searchQuery)) {
            throw new IllegalArgumentException("SearchQuery field can not be Empty or Blank!");
        }
        solrQuery.setQuery(searchQuery);

        if(facetFields != null && !facetFields.isEmpty()){
            solrQuery.setFacet(true);
            solrQuery.setFacetMinCount(1);
            for(String facetField : facetFields){
                solrQuery.addFacetField(facetField);
            }
        }

        logger.debug("QueryRequest solrQuery: " + solrQuery);

        SolrRequest request = new QueryRequest(solrQuery, METHOD.POST);

        QueryResponse response = new QueryResponse(productSkuSolrServer.request(request), productSkuSolrServer);
        
        logger.debug("QueryResponse Obj: " + response.toString());
        
        List<ProductSkuSolr> solrList = response.getBeans(ProductSkuSolr.class);
        ProductSkuResponseBean productResponseBean = new ProductSkuResponseBean();
        productResponseBean.setProductSkuSolrList(solrList);
        
        if(facetFields != null && !facetFields.isEmpty()){
            List<FacetField> solrFacetFieldList = response.getFacetFields();
            productResponseBean.setProductSkuSolrFacetFieldList(solrFacetFieldList);
        }
        return productResponseBean;
    }
    
    @Deprecated
    public ProductSkuResponseBean searchProductSku(String searchBy, String searchText, List<String> facetFields) throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setParam("rows", getMaxResult());
        
        if (StringUtils.isEmpty(searchBy)) {
            throw new IllegalArgumentException("SearchBy field can not be Empty or Blank!");
        }

        if (StringUtils.isEmpty(searchText)) {
            solrQuery.setQuery(searchBy + ":*");
        } else {
            solrQuery.setQuery(searchBy + ":" + searchText + "*");
        }

        if(facetFields != null && !facetFields.isEmpty()){
            solrQuery.setFacet(true);
            solrQuery.setFacetMinCount(1);
            for(String facetField : facetFields){
                solrQuery.addFacetField(facetField);
            }
        }

        logger.debug("QueryRequest solrQuery: " + solrQuery);

        SolrRequest request = new QueryRequest(solrQuery, METHOD.POST);

        QueryResponse response = new QueryResponse(productSkuSolrServer.request(request), productSkuSolrServer);
        
        logger.debug("QueryResponse Obj: " + response.toString());
        
        List<ProductSkuSolr> solrList = response.getBeans(ProductSkuSolr.class);
        ProductSkuResponseBean productResponseBean = new ProductSkuResponseBean();
        productResponseBean.setProductSkuSolrList(solrList);
        
        if(facetFields != null && !facetFields.isEmpty()){
            List<FacetField> solrFacetFieldList = response.getFacetFields();
            productResponseBean.setProductSkuSolrFacetFieldList(solrFacetFieldList);
        }
        return productResponseBean;
    }

    public ProductSkuResponseBean searchProductSku() throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setParam("rows", getMaxResult());
        
        solrQuery.setQuery("*");
        solrQuery.setFacet(true);
        solrQuery.setFacetMinCount(1);
        solrQuery.addFacetField("name");
        solrQuery.addFacetField("code");

        logger.debug("QueryRequest solrQuery: " + solrQuery);
        
        SolrRequest request = new QueryRequest(solrQuery, METHOD.POST);
        
        QueryResponse response = new QueryResponse(productSkuSolrServer.request(request), productSkuSolrServer);

        logger.debug("QueryResponse Obj: " + response.toString());
        
        List<ProductSkuSolr> solrList = response.getBeans(ProductSkuSolr.class);
        List<FacetField> solrFacetFieldList = response.getFacetFields();
        
        ProductSkuResponseBean productSkuResponseBean = new ProductSkuResponseBean();
        productSkuResponseBean.setProductSkuSolrList(solrList);
        productSkuResponseBean.setProductSkuSolrFacetFieldList(solrFacetFieldList);
        
        return productSkuResponseBean;
    }

}
