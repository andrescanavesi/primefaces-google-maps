/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.xpot.primefaces.google.maps;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

/**
 *
 * @author Andres Canavesi
 */
@Named("autocompleteManagedBean")
@ViewScoped
public class AutocompleteManagedBean implements Serializable {

    private String address;
    /**
     * Get your from https://console.developers.google.com
     */
    private String googleMapsApiKey = "YOUR_API_KEY";
    private static final Logger LOG = Logger.getLogger(AutocompleteManagedBean.class.getName());
    /**
     * The key is the formatted given by google map
     */
    private Map<String, GeocodingResult> placesMap;
    private MapModel resultMapModel;
    private GeocodingResult selectedPlace;
    private Double centerMapLatitute = -34.9011127;
    private Double centerMapLongitude = -56.16453139999999;
    private GeoApiContext geoApiContext;

    @PostConstruct
    public void init() {
        LOG.log(Level.INFO, "Init {0}", AutocompleteManagedBean.class.getName());
        geoApiContext = new GeoApiContext().setApiKey(googleMapsApiKey);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public MapModel getResultMapModel() {
        return resultMapModel;
    }

    public GeocodingResult getSelectedPlace() {
        return selectedPlace;
    }

    public Double getCenterMapLatitute() {
        return centerMapLatitute;
    }

    public Double getCenterMapLongitude() {
        return centerMapLongitude;
    }

    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }

    public void setGoogleMapsApiKey(String googleMapsApiKey) {
        this.googleMapsApiKey = googleMapsApiKey;
    }

    /**
     *
     * @param message
     */
    protected void showInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, ""));
    }

    /**
     *
     * @param message
     */
    protected void showWarnMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, message, ""));
    }

    /**
     *
     * @param message
     */
    protected void showErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, ""));
    }

    /**
     *
     * @param query
     * @return
     */
    public List<String> search(String query) {
        List<String> results = new ArrayList<>();
        try {
            GeocodingResult[] places = GeocodingApi.geocode(geoApiContext, query).await();
            placesMap = new HashMap<>();

            for (GeocodingResult place : places) {
                results.add(place.formattedAddress);
                placesMap.put(place.formattedAddress, place);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            results.add("Error " + e.getMessage());
        }
        return results;

    }

    public void onItemSelect(SelectEvent event) {
        try {
            selectedPlace = placesMap.get(event.getObject().toString());
            for (AddressComponent addressComponent : selectedPlace.addressComponents) {
                LOG.log(Level.INFO, "Address component long name: {0}. Short name{1}", new Object[]{addressComponent.longName, addressComponent.shortName});
                for (AddressComponentType type : addressComponent.types) {
                    LOG.log(Level.INFO, "\tAddress component type: {0}", type);
                }
            }
            for (AddressType type : selectedPlace.types) {
                LOG.log(Level.INFO, "Address type: {0}", type);
            }
            if (selectedPlace.postcodeLocalities != null) {
                for (String postcodeLocality : selectedPlace.postcodeLocalities) {
                    LOG.log(Level.INFO, "Postal code: {0}", postcodeLocality);
                }
            }

            LOG.log(Level.INFO, "Location type: {0}", selectedPlace.geometry.locationType);

            centerMapLatitute = selectedPlace.geometry.location.lat;
            centerMapLongitude = selectedPlace.geometry.location.lng;

            resultMapModel = new DefaultMapModel();
            LatLng coord1 = new LatLng(selectedPlace.geometry.location.lat, selectedPlace.geometry.location.lng);
            resultMapModel.addOverlay(new Marker(coord1, selectedPlace.formattedAddress));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            showErrorMessage(e.getMessage());
        }

    }

}
