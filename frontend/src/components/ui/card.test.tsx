import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "./card";

describe("Card Components", () => {
  it("should render all card components", () => {
    render(
      <Card data-testid="card">
        <CardHeader data-testid="header">
          <CardTitle data-testid="title">Title</CardTitle>
          <CardDescription data-testid="description">
            Description
          </CardDescription>
        </CardHeader>
        <CardContent data-testid="content">Content</CardContent>
        <CardFooter data-testid="footer">Footer</CardFooter>
      </Card>,
    );

    expect(screen.getByTestId("card")).toBeInTheDocument();
    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("title")).toBeInTheDocument();
    expect(screen.getByTestId("description")).toBeInTheDocument();
    expect(screen.getByTestId("content")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
  });

  it("should not render CardTitle when it has no content", () => {
    const { container } = render(<CardTitle />);
    expect(container.firstChild).toBeNull();
  });

  it("should render CardTitle when it has children", () => {
    render(<CardTitle>Title Content</CardTitle>);
    expect(screen.getByText("Title Content")).toBeInTheDocument();
  });

  it("should render CardTitle when it has aria-label", () => {
    render(<CardTitle aria-label="Accessible Title" />);
    expect(screen.getByLabelText("Accessible Title")).toBeInTheDocument();
  });
});
