# Cloavy Android Real VPN Test v2

This build keeps the Cloavy UI and adds the first real WireGuard Android tunnel test.

Important:
- Do not paste private keys into ChatGPT.
- Do not hardcode production VPN configs into the app.
- For this test, export a client .conf from wg-easy and import it inside the app on the Android device.

## Test flow

1. In wg-easy, create a client or use `cloavy-android-test`.
2. Download the client `.conf` file.
3. Transfer the `.conf` file to the Android phone.
4. Open Cloavy VPN.
5. On the Home screen, tap `Import` in the Real VPN test card.
6. Select the `.conf` file.
7. Tap the main Protect button.
8. Android will show a VPN permission dialog.
9. Allow the VPN connection.
10. Check IP in the browser. It should show your Contabo server provider/location.

## Current scope

Working:
- Real Android VpnService permission.
- WireGuard tunnel library dependency.
- Import `.conf` locally on device.
- Connect and disconnect real tunnel.
- Existing 4 Cloavy custom themes.
- RU/EN toggle from mock UI.

Not included yet:
- Backend.
- Payments.
- Auto peer provisioning.
- Secure encrypted config storage.
- Real account/device limits.
- Google Play production declarations.

## Dependency

Uses `com.wireguard.android:tunnel:1.0.20260102` from Maven Central.
