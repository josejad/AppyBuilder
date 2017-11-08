// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.util.Log;
import com.appybuilder.iab.v3.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// The library is from here: https://github.com/anjlab/android-inapp-billing-v3

/**
 * InAppBilling component lets you sell digital content from inside your applications. It can be used to sell
 * a wide range of content, including downloadable content such as media files or photos, virtual content
 * such as game levels or potions, premium services and features, and more.
 * You can use In-app Billing to sell products as:
 * <ul>
 *   <li>Standard in-app products (one-time billing), or</li>
 *   <li>Subscriptions (recurring, automated billing)</li>
 * </ul>
 * For more info, please refer http://developer.android.com/google/play/billing/billing_admin.html
 " and here: http://developer.android.com/google/play/billing/billing_testing.html
 */
@DesignerComponent(version = YaVersion.IN_APP_COMPONENT_VERSION,
        category = ComponentCategory.MONETIZE,
        description = "<p>InAppBilling component lets you sell digital content from inside your applications. " +
                "It can be used to sell a wide range of content, including downloadable content such as media files " +
                "or photos, virtual content such as game levels or potions, premium services and features, and more. " +
                "You can use In-app Billing to sell products as: " +
                "<ul>" +
                " <li>Standard in-app products (one-time billing), or</li>" +
                " <li>Subscriptions (recurring, automated billing)</li>" +
                "</ul>" +
                "<p> For more information, please see here: " +
                "http://tinyurl.com/m3acuk3" +
                " and here: http://tinyurl.com/cl7nyuv",
        nonVisible = true,
        iconName = "images/billing.png")
@SimpleObject
@UsesLibraries(libraries = "appybuilder-billing3-1.0.32.jar")
@UsesPermissions(permissionNames = "com.android.vending.BILLING")
public class InAppBilling extends AndroidNonvisibleComponent implements Component, BillingProcessor.IBillingHandler {

  BillingProcessor billingProcessor;
  private String licenseKey="LICENSE_KEY";

  private final ComponentContainer container;
  private String LOG_TAG = "InAppBilling";

  // https://developer.android.com/google/play/billing/billing_testing.html
  // https://developer.android.com/google/play/billing/billing_subscriptions.html
  // android.test.purchased, android.test.canceled, android.test.refunded, android.test.item_unavailable
  /**
   * Creates a new Button component.
   *
   * @param container container, component will be placed in
   */
  public InAppBilling(ComponentContainer container) {
    super(container.$form());
    // Save the container for later
    this.container = container;

    initBillingProcessor();

//    LicenseKey(licenseKey);
  }

  private void initBillingProcessor() {
    // NOTE: SINCE THIS IS A NON-VISUAL COMPONENT AND DOESN'T HAVE onActivityResult, I INSTANTIATED
    // billingProcessor in $form. Then on the $form.onActivityResult, I check to see if billingProcessor != null
    // IF NOT NULL, IT MEANS THAT WE ARE DEALING WITH InAppBilling COMPONENT. IN THIS SCENARIO, THE $form WILL
    // HANDLE billingProcessor.handleActivityResult
    billingProcessor = new BillingProcessor(container.$context(), licenseKey, this);
    container.$form().billingProcessor = billingProcessor;
  }

  //  @DesignerProperty(defaultValue = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhS3tSePbTrtGYfXkmf9JYog0ffRmNlVTKkfBjmNlT7aB371hT6h8qsxZtNBaNB3fAb7WULHazPZcPB1m2bevQE/RvnRU/zufrDdF8gXbnMNbgDAc1a1cEdCLy/NclABy22bN+wbqGP570AWHw8IoacDBv5jjRQzfWMbYVwcOC8CKh6LiiJAsvaSuXJvxdiKD2pv0Qp0/hBd1AAQH+U7pRsrfUlSjkaMaX0m4jCi7q4UfFBy0ne3sxeBcjnGqQlGMS227qXM2kAVyJGc83ndTEjsHHiMrERCWkqcOlnt02dmhEIqFaaw0l2Gc2ZvbJ6pjYFEHUiQwWgSHcmQzDeSGrQIDAQAB",
  @DesignerProperty(defaultValue = "LICENSE_KEY", editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void LicenseKey(String licenseKey) {
    this.licenseKey = licenseKey;
    initBillingProcessor();
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String LicenseKey() {
    return this.licenseKey;
  }


  @SimpleFunction(description = "Starts the purchase for the Product ID from Google Play Console")
  public void StartPurchase(String productId) {
    if (billingProcessor == null) {
      LicenseKey(licenseKey);
    }

    billingProcessor.purchase(container.$form(), productId);
  }

  @SimpleFunction(description = "Use to determine if product has been purchased this product")
  public boolean IsPurchased(String productId) {
    if (billingProcessor == null) {
      LicenseKey(this.licenseKey);
      return false;
    }

    return billingProcessor.isPurchased(productId);
  }

  @SimpleFunction(description = "Use to determine if user has subscribed to this product")
  public boolean IsSubscribed(String productId) {
    if (billingProcessor == null) {
      LicenseKey(this.licenseKey);
      return false;
    }

    return billingProcessor.isSubscribed(productId);
  }

  @SimpleFunction(description = "Retrieves list of owned products")
  public YailList ListOwnedProducts() {
    if (billingProcessor == null) {
      LicenseKey(this.licenseKey);
      return YailList.makeEmptyList();
    }

    List<String> items = billingProcessor.listOwnedProducts();
    if (items == null ) {
      return YailList.makeEmptyList();
    } else {
      return YailList.makeList(items);
    }
  }

  @SimpleFunction(description = "Retrieves list of owned subscriptions")
  public YailList ListOwnedSubscriptions() {
    if (billingProcessor == null) {
      LicenseKey(this.licenseKey);
      return YailList.makeEmptyList();
    }

    List<String> items = billingProcessor.listOwnedSubscriptions();
    if (items == null ) {
      return YailList.makeEmptyList();
    } else {
      return YailList.makeList(items);
    }
  }

  @SimpleFunction(description = "Starts the subscription for the Subscription (Product) ID from Google Play Console")
  public void StartSubscription(String productId) {
    if (billingProcessor == null) {
      LicenseKey(licenseKey);
    }
   billingProcessor.subscribe(container.$form(), productId);
  }

  @SimpleFunction(description = "Get Product details for the Product ID")
  public YailList GetPurchaseListingDetails(String productId) {
    if (billingProcessor == null) {
      initBillingProcessor();
    }
    return buildSkuDetail(billingProcessor.getPurchaseListingDetails(productId));
  }

  @SimpleFunction(description = "Get subscription detail for the Subscription (product) ID")
  public YailList GetSubscriptioListingDetails(String productId) {
    if (billingProcessor == null) {
      initBillingProcessor();
    }

    return buildSkuDetail(billingProcessor.getSubscriptionListingDetails(productId));
  }

  @SimpleFunction(description = "Get transaction detail for the product ID. " +
          "Will return empty list if transaction for purchase doesn't exist")
  public YailList GetPurchaseTransactionDetails(String productId) {
    if (billingProcessor == null) {
      initBillingProcessor();
    }

    // Should return empty list if transaction for product doesn't exist
    TransactionDetails transactionDetails = billingProcessor.getPurchaseTransactionDetails(productId);
    return buildTransactionDetail(transactionDetails);
  }

  @SimpleFunction(description = "Get transaction detail for the Subscription (product) ID. " +
          "Will return empty list if transaction for subscription doesn't exist")
  public YailList GetSubscriptionTransactionDetails(String productId) {
    if (billingProcessor == null) {
      initBillingProcessor();
    }

    // Should return empty list if transaction for subscription doesn't exist
    TransactionDetails transactionDetails = billingProcessor.getSubscriptionTransactionDetails(productId);
    return buildTransactionDetail(transactionDetails);
  }

  @SimpleFunction(description = "Consume the last purchase of the given product to allow re-purchase of this item.")
  public boolean ConsumePurchase(String productId) {
    if (billingProcessor == null) {
      initBillingProcessor();
    }

   return billingProcessor.consumePurchase(productId);
  }

  @SimpleFunction(description = "Since Google's doesn't provide any callbacks to handle canceled " +
          "and/or expired subscriptions you have to handle it on your own by running this block periodically. " +
          "Executing this will restore purchases & subscriptions.")
  public boolean LoadOwnedPurchasesFromGoogle() {
    if (billingProcessor == null) {
      initBillingProcessor();
    }

   return billingProcessor.loadOwnedPurchasesFromGoogle();
  }



  private YailList buildSkuDetail(SkuDetails skuDetails) {
    if (billingProcessor == null ||skuDetails == null) {
      return YailList.makeEmptyList();
    }
    ArrayList<String> myList = new ArrayList<String>();

    myList.add("productId=" + skuDetails.productId);
    myList.add("title=" + skuDetails.title);
    myList.add("description=" + skuDetails.description);
    myList.add("isSubscription=" + skuDetails.isSubscription);
    myList.add("priceText=" + skuDetails.priceText);
    myList.add("currency=" + skuDetails.currency);

    YailList yailList = YailList.makeList(myList);

    return yailList;
  }
  private YailList buildTransactionDetail(TransactionDetails tranDetails) {
    if (billingProcessor == null ||tranDetails == null) {
      return YailList.makeEmptyList();
    }
    ArrayList<String> myList = new ArrayList<String>();

    myList.add("productId=" + tranDetails.productId);
    myList.add("orderId=" + tranDetails.orderId);
    myList.add("purchaseDateTime=" + formatDate(tranDetails.purchaseTime));

    PurchaseInfo purchaseInfo = tranDetails.purchaseInfo;
    if (purchaseInfo != null) {
      PurchaseInfo.ResponseData responseData= purchaseInfo.parseResponseData();
      if (responseData != null) {
        myList.add("purchaseStateName=" + responseData.purchaseState.name());
        myList.add("purchaseState=" + responseData.purchaseState);
      }

      // This is too much data. Returns prodid, orderId, token, package, timestamp
//      myList.add("responseData=" + purchaseInfo.responseData);
    }

    YailList yailList = YailList.makeList(myList);

    return yailList;
  }

  // IBillingHandler implementation

  /**
   * Called when BillingProcessor was initialized and it's ready to purchase
   */
  @Override
  public void onBillingInitialized() {
    Log.i(LOG_TAG, "onBillingInitialized" );

      List<String> ownedProducts = billingProcessor.listOwnedProducts();
      List<String> ownedSubscriptions = billingProcessor.listOwnedSubscriptions();


    // Billing has been initialized. Invoke our SimpleEvent
    BillingInitialized(true, YailList.makeList(ownedProducts), YailList.makeList(ownedSubscriptions));
  }

  @SimpleEvent(description = "Triggered when Google Play In-App Billing is initialized")
  public boolean BillingInitialized(boolean isReadyToPurchase, YailList ownedProducts, YailList ownedSubscriptions) {
    return EventDispatcher.dispatchEvent(this, "BillingInitialized", isReadyToPurchase, ownedProducts, ownedSubscriptions);
  }

  @SimpleEvent(description = "Triggered when a product was purchased")
  public boolean ProductPurchased(String prodId, String orderId, boolean isAutoRenewing, String purchaseDateTime) {
    return EventDispatcher.dispatchEvent(this, "ProductPurchased", prodId, orderId, isAutoRenewing, purchaseDateTime);
  }

  @SimpleEvent(description = "Triggered when a billing error occurs")
  public boolean BillingError(int errorCode, String message) {
    return EventDispatcher.dispatchEvent(this, "BillingError", errorCode, message);
  }

  @SimpleEvent(description = "Triggered when purchase history was restored and the list of " +
          "all owned Product or Subscription ID's was loaded from Google Play")
  public void PurchaseHistoryRestored(YailList purchaseHistory) {
    EventDispatcher.dispatchEvent(this, "PurchaseHistoryRestored", purchaseHistory);
  }

  /*
   * Called when requested PRODUCT ID was successfully purchased
   */
  @Override
  public void onProductPurchased(String productId, TransactionDetails details) {
    Log.i(LOG_TAG, "onProductPurchased transaction details are: " + details);
    String prodId = details.productId;
    String orderId = details.orderId;
//    String purchaseToken = details.purchaseToken;
    PurchaseInfo purchaseInfo = details.purchaseInfo;
//    purchaseInfo.PurchaseState.name
    PurchaseInfo.ResponseData responseData = purchaseInfo.parseResponseData();
    boolean isAutoRenewing = responseData.autoRenewing;
    Date purchaseTime = details.purchaseTime;

    ProductPurchased(prodId, orderId, isAutoRenewing, formatDate(purchaseTime));
  }

  @Override
  public void onPurchaseHistoryRestored() {
    Log.i(LOG_TAG, "onPurchaseHistoryRestored" );
    if (billingProcessor == null) {
      return;
    }
    /*
     * Called when purchase history was restored and the list of all owned PRODUCT ID's
     * was loaded from Google Play
     */
//    YailList ownedSkus = YailList.makeEmptyList();
    List<String> ownedSkus = new ArrayList<String>();
    for(String sku : billingProcessor.listOwnedProducts()) {
      ownedSkus.add(sku);
    }
    for(String sku : billingProcessor.listOwnedSubscriptions()) {
      ownedSkus.add(sku);
    }

    PurchaseHistoryRestored(YailList.makeList(ownedSkus));
  }

  private String formatDate(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    return dateFormat.format(date);
  }
  /*
   * Called when some error occurred. See Constants class for more details -- com.anjlab.android.iab.v3.Constants
   */
  @Override
  public void onBillingError(int errorCode, Throwable error) {
    Log.i(LOG_TAG, "onBillingError" );
    BillingError(errorCode, Constants.BILLING_ERROR.get(errorCode));
  }



  // If this library is creating issues, see if you can implement using this:
  // http://www.theappguruz.com/tutorial/implement-in-app-purchase-version-3/  github: https://github.com/tejas123/In-App-Purchase-version-3
  // also check this: theappguruz
//  public void onActivityResult(int requestCode, int resultCode, Intent data) {
//    Log.i(LOG_TAG, "resultReturned2 - resultCode = " + resultCode);
//    if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
//      Log.i(LOG_TAG, "resultReturned2 - resultCode2 = " + resultCode);
//      container.$form().onActivityResult(requestCode, resultCode, data);
//    }
//  }
//
//  public void resultReturned(int requestCode, int resultCode, Intent data) {
//    Log.i(LOG_TAG, "resultReturned - resultCode = " + resultCode);
//    if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
//      Log.i(LOG_TAG, "resultReturned - resultCode2 = " + resultCode);
//      container.$form().onActivityResult(requestCode, resultCode, data);
//    }
//  }
}
