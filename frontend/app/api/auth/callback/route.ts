import { NextRequest, NextResponse } from "next/server";

export const runtime = 'edge';
export const dynamic = "force-dynamic";

export async function GET(request: NextRequest) {
  const searchParams = request.nextUrl.searchParams;
  const token = searchParams.get("token");
  const redirect = searchParams.get("redirect") || "/dashboard";

  if (!token) {
    return NextResponse.redirect(new URL("/login?error=missing_token", request.url));
  }

  // Create response with redirect
  const response = NextResponse.redirect(new URL(redirect, request.url));

  // Set JWT cookie for Vercel domain
  response.cookies.set("jwt", token, {
    httpOnly: true,
    secure: true, // HTTPS only
    sameSite: "lax", // Can be "lax" for same-site, "none" requires secure
    maxAge: 24 * 60 * 60, // 1 day
    path: "/",
  });

  return response;
}
