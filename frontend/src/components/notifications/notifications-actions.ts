"use server";

import { getVapidPublicKey, registerSubscription } from "@/generated";

export async function getVapidPublicKeyAction() {
  return (await getVapidPublicKey()).data?.publicKey;
}

export async function registerSubscriptionAction(
  endpoint: string,
  p256dh: string,
  auth: string,
) {
  await registerSubscription({ body: { endpoint, keys: { p256dh, auth } } });
}
