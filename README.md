# Kotlin Weather App

This is a weather application built with Kotlin for Android. The application fetches real-time
weather data from the OpenWeatherMap API and displays it in a user-friendly interface.

## Features

- Displays current weather information including temperature, humidity, wind speed, and air quality
  index.
- Provides hourly weather forecast.
- Supports widgets to display city name, current time, temperature, and weather icon on the home
  screen.
- Automatically updates weather data every 15 minutes using WorkManager.
- Handles location permissions and GPS status.

## Permissions

The application requires the following permissions:

- `ACCESS_FINE_LOCATION`: To get the device's current location.
- `ACCESS_COARSE_LOCATION`: To get the device's current location in a battery-saving manner.
- `INTERNET`: To make network requests to the OpenWeatherMap API.
- `SCHEDULE_EXACT_ALARM`: To schedule precise and inexact alarms for updating the weather data.
- `RECEIVE_BOOT_COMPLETED`: To schedule alarms after the device has booted.

## Usage

Clone the repository and import the project into Android Studio. You will need to provide your own
OpenWeatherMap API key in the `MainActivity.kt` file.

```kotlin
private val _openWeatherMapApiAky = "your_api_key_here"
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the terms of the [MIT license](https://github.com/nureka-rodrigo/Kotlin-Weather-App/blob/main/LICENSE).